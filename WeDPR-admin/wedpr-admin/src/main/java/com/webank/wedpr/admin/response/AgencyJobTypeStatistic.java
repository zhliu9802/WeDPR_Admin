package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/9/11 22:28 */
@Data
public class AgencyJobTypeStatistic {
    private String agencyName;
    private Integer totalCount;
    private List<JobTypeStatistic> jobTypeStatistic;
}
