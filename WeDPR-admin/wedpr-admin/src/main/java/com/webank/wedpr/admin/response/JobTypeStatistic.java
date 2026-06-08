package com.webank.wedpr.admin.response;

import lombok.Data;

/** Created by caryliao on 2024/9/11 22:27 */
@Data
public class JobTypeStatistic {
    private String jobType;
    private String jobTypeTitle;
    private Integer count;
}
