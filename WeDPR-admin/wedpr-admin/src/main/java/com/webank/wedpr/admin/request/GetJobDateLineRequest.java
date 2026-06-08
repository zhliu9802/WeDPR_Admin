package com.webank.wedpr.admin.request;

import lombok.Data;

@Data
public class GetJobDateLineRequest {
    private String startTime;
    private String endTime;
}
