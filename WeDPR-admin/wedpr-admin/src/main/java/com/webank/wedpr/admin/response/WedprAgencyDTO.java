package com.webank.wedpr.admin.response;

import java.time.LocalDateTime;
import lombok.Data;

/** Created by caryliao on 2024/8/23 21:49 */
@Data
public class WedprAgencyDTO {
    private String agencyId;
    private String agencyName;
    private String agencyContact;
    private String contactPhone;
    private LocalDateTime createTime;
    private Integer userCount;
    private Integer certStatus;
    private Integer agencyStatus;
}
