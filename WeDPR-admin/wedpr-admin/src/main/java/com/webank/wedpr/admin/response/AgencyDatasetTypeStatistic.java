package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;

@Data
public class AgencyDatasetTypeStatistic {
    private String agencyName;
    private List<DatasetTypeStatistic> datasetTypeStatistic;
    private int totalCount;
}
