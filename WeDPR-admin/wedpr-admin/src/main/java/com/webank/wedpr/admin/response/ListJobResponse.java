package com.webank.wedpr.admin.response;

import com.webank.wedpr.admin.entity.WedprJobTable;
import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/9/5 9:35 */
@Data
public class ListJobResponse {
    private Long total;
    private List<WedprJobTable> jobList;
}
