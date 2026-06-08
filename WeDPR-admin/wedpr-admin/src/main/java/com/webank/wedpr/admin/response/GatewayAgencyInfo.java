package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;
import lombok.ToString;

/** Created by caryliao on 2024/9/14 11:00 */
@Data
@ToString
public class GatewayAgencyInfo {
    private String agency;
    private List<GatewayAgencyPeer> peers;
}
