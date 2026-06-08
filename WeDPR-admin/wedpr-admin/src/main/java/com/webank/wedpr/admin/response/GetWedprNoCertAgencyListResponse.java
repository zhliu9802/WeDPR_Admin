package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/8/22 23:23 */
@Data
public class GetWedprNoCertAgencyListResponse {
    private List<WedprAgencyWithoutCertDTO> agencyList;
}
