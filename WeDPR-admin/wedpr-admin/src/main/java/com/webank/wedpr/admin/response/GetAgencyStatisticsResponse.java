package com.webank.wedpr.admin.response;

import java.util.Set;
import lombok.Data;

/** Created by caryliao on 2024/9/10 9:26 */
@Data
public class GetAgencyStatisticsResponse {
    private String agencyAdmin;
    private Integer totalAgencyCount;
    private Integer faultAgencyCount;
    private Set<String> agencyPeerList;
    private Set<String> agencyFaultList;
}
