package com.lululemon.flow.data.transfer.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.lululemon.flow.data.transfer.service.JobService;
import com.lululemon.flow.data.transfer.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/job")
@Slf4j
public class JobController {


    private JobService jobService;

    @VisibleForTesting
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Method for processing sql. Accept json as an input
     * <pre>
     *     {
     *         "sqlPath":"path_to_sql_file"
     *     }
     * </pre>
     * Returns Job response structure
     * <pre>
     *     {
     *         "jobId": 10,
     *         "executionId": 12,
     *         "code": 200,
     *         "status":
     *         "message":""
     *     }
     * </pre>
     *
     * @param request - json where path to the sql file is specified
     * @return JobResponse json structure which describes job execution status
     */
    @PostMapping("/transform")
    public JobResponse transform(@RequestBody JobRequest.Transform request) {
        Utils.logMdc("cid", "cid=" + UUID.randomUUID().toString());
        log.info("Transform request: {}", requestAsJson(request));
        Preconditions.checkNotNull(request.getSqlPath(), "sqlPath parameter cannot be null");
        JobExecution je = jobService.runJob(request);
        return buildJobResponse(je);
    }

    @GetMapping("/{id}")
    public JobResponse status(@PathVariable("id") Long jobId) {
        return buildJobResponse(jobService.getJobExecution(jobId));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<JobResponse> handleException(Exception e) {
        log.error("Batch job exception", e);
        return new ResponseEntity<>(buildJobResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @VisibleForTesting
    JobResponse buildJobResponse(JobExecution execution) {
        return JobResponse.builder()
                .jobId(execution.getJobId())
                .executionId(execution.getId())
                .code(execution.getExitStatus().getExitCode())
                .status(execution.getStatus().toString())
                .message(execution.getExitStatus().getExitDescription())
                .build();
    }

    @VisibleForTesting
    JobResponse buildJobResponse(Exception exception) {
        return JobResponse.builder()
                .code("FAILED")
                .status("FAILED")
                .message(Throwables.getStackTraceAsString(exception))
                .build();
    }

    @VisibleForTesting
    static String requestAsJson(JobRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Exception caught:", e);
        }
        return request.toString();
    }
}
