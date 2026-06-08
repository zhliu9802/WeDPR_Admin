package com.webank.wedpr.admin.request;

import lombok.Data;

@Data
public class GetDatasetDateLineRequest {
    private String startTime;
    private String endTime;
}
