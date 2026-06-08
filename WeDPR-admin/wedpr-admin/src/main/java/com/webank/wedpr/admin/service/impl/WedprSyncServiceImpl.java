package com.webank.wedpr.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.admin.common.Utils;
import com.webank.wedpr.admin.request.GetWedprAuditLogListRequest;
import com.webank.wedpr.admin.service.WedprSyncService;
import com.webank.wedpr.components.sync.dao.ResourceActionDO;
import com.webank.wedpr.components.sync.dao.SyncStatusMapper;
import com.webank.wedpr.components.sync.service.impl.ResourceStatusResult;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Created by caryliao on 2024/9/1 23:11 */
@Service
public class WedprSyncServiceImpl extends ServiceImpl<SyncStatusMapper, ResourceActionDO>
        implements WedprSyncService {
    @Override
    public ResourceStatusResult queryRecordSyncStatus(GetWedprAuditLogListRequest request) {
        QueryWrapper<ResourceActionDO> queryWrapper = new QueryWrapper<>();
        String ownerAgencyName = request.getOwnerAgencyName();
        String resourceAction = request.getResourceAction();
        String resourceType = request.getResourceType();
        String status = request.getStatus();
        String startTimeStr = request.getStartTime();
        String endTimeStr = request.getEndTime();
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        if (!StringUtils.isEmpty(ownerAgencyName)) {
            queryWrapper.like("agency", ownerAgencyName);
        }
        if (!StringUtils.isEmpty(resourceAction)) {
            queryWrapper.eq("resource_action", resourceAction);
        }
        if (!StringUtils.isEmpty(resourceType)) {
            queryWrapper.eq("resource_type", resourceType);
        }
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(startTimeStr)) {
            LocalDateTime startTime = Utils.getLocalDateTime(startTimeStr);
            queryWrapper.ge("create_time", startTime);
        }
        if (!StringUtils.isEmpty(endTimeStr)) {
            LocalDateTime endTime = Utils.getLocalDateTime(endTimeStr);
            queryWrapper.le("create_time", endTime);
        }
        queryWrapper.orderByDesc("last_update_time");
        queryWrapper.select(
                "resource_id",
                "status",
                "create_time",
                "last_update_time",
                "status_msg",
                "agency",
                "resource_type",
                "resource_action",
                "`index`",
                "block_number",
                "transaction_hash",
                "`trigger`");
        Page<ResourceActionDO> ResourceActionDOPage = new Page<>(pageNum, pageSize);
        Page<ResourceActionDO> page = page(ResourceActionDOPage, queryWrapper);
        ResourceStatusResult resourceStatusResult = new ResourceStatusResult();
        resourceStatusResult.setTotal(page.getTotal());
        resourceStatusResult.setDataList(page.getRecords());
        return resourceStatusResult;
    }
}
