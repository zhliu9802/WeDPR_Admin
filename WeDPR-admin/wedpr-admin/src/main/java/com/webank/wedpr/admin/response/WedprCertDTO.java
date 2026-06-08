package com.webank.wedpr.admin.response;

import java.time.LocalDateTime;
import lombok.Data;

/** Created by caryliao on 2024/8/23 21:49 */
@Data
public class WedprCertDTO {
    private String certId;
    private String agencyId;
    private String agencyName;
    private LocalDateTime signTime;
    private LocalDateTime expireTime;
    private Integer certStatus;
    private Integer enable;
}
