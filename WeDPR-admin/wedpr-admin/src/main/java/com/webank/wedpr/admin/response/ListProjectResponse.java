package com.webank.wedpr.admin.response;

import com.webank.wedpr.admin.entity.WedprProjectTable;
import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/9/5 9:35 */
@Data
public class ListProjectResponse {
    private Long total;
    private List<WedprProjectTable> projectList;
}
