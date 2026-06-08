package com.webank.wedpr.admin.response;

import lombok.Data;

/** Created by caryliao on 2024/9/11 22:30 */
@Data
public class JobOverview {
    private Integer successCount;
    private Integer totalCount;
    private String successProportion;
}
