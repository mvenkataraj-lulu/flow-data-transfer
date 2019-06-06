package com.lululemon.flow.data.transfer.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long jobId;
    private Long executionId;
    private String code;
    private String status;
    private String message;
}
