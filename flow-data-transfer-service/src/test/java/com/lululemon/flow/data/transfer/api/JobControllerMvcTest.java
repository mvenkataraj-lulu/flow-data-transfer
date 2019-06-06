package com.lululemon.flow.data.transfer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = JobController.class, secure = false)
@ContextConfiguration(classes = {MvcTestConfig.class})
public class JobControllerMvcTest {

    @MockBean
    private JobController jobController;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getJobStatus() throws Exception {
        JobExecution expected = new JobExecution(1L);
        expected.setId(1L);
        expected.setExitStatus(ExitStatus.COMPLETED);
        expected.setStatus(BatchStatus.COMPLETED);
        expected.setEndTime(new Date());
        JobResponse response = buildJobResponse(expected);

        Mockito.when(jobController.status(anyLong())).thenReturn(response);

        MvcResult result = this.mockMvc.perform(get("/job/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(mapper.writeValueAsString(response)))).andReturn();

    }

    @Test
    @Ignore
    public void transform() throws Exception {
        JobExecution expected = new JobExecution(1L);
        expected.setId(1L);
        expected.setExitStatus(ExitStatus.COMPLETED);
        expected.setStatus(BatchStatus.COMPLETED);
        expected.setEndTime(new Date());
        JobResponse response = buildJobResponse(expected);


        JobRequest.Transform request = new JobRequest.Transform();
        request.setSqlPath("/path");

        Mockito.when(jobController.transform(any(JobRequest.Transform.class))).thenReturn(response);

        this.mockMvc.perform(post("/job/transform").content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(mapper.writeValueAsString(response)))).andReturn();
    }

    private JobResponse buildJobResponse(JobExecution execution) {
        return JobResponse.builder()
                .jobId(execution.getJobId())
                .executionId(execution.getId())
                .code(execution.getExitStatus().getExitCode())
                .status(execution.getStatus().toString())
                .message(execution.getExitStatus().getExitDescription())
                .build();
    }
}
