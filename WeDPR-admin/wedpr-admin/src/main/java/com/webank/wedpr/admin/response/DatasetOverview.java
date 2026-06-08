package com.webank.wedpr.admin.response;

import lombok.Data;

@Data
public class DatasetOverview {
    private Integer usedCount;
    private Integer totalCount;
    private String usedProportion;
}
