package com.lululemon.flow.data.transfer.api;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class JobRequest {

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Transform extends JobRequest {

        private String sqlPath;
    }

    public enum JobType {
        TRANSFORM
    }
}
