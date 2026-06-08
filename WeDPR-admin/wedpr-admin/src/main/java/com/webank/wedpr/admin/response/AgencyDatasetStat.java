package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/9/11 9:59 */
@Data
public class AgencyDatasetStat {
    private String agencyName;
    private List<String> dateList;
    private List<Integer> countList;
}
