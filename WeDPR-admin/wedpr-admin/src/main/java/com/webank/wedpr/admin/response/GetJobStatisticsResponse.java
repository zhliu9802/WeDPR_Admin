package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/9/10 9:26 */
@Data
public class GetJobStatisticsResponse {
    private JobOverview jobOverview;
    private List<JobTypeStatistic> jobTypeStatistic;
    private List<AgencyJobTypeStatistic> agencyJobTypeStatistic;
}
