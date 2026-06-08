package com.webank.wedpr.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.admin.common.Utils;
import com.webank.wedpr.admin.entity.WedprProjectTable;
import com.webank.wedpr.admin.mapper.WedprProjectTableMapper;
import com.webank.wedpr.admin.request.GetWedprProjectListRequest;
import com.webank.wedpr.admin.response.ListProjectResponse;
import com.webank.wedpr.admin.service.WedprProjectTableService;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-09-04
 */
@Service
public class WedprProjectTableServiceImpl
        extends ServiceImpl<WedprProjectTableMapper, WedprProjectTable>
        implements WedprProjectTableService {

    @Autowired private WedprProjectTableMapper wedprProjectTableMapper;

    @Override
    public ListProjectResponse listProject(GetWedprProjectListRequest request) {
        LambdaQueryWrapper<WedprProjectTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String ownerAgencyName = request.getOwnerAgency();
        String projectId = request.getId();
        String projectName = request.getName();
        String startTimeStr = request.getStartTime();
        String endTimeStr = request.getEndTime();
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        if (!StringUtils.isEmpty(ownerAgencyName)) {
            lambdaQueryWrapper.like(WedprProjectTable::getOwnerAgency, ownerAgencyName);
        }
        if (!StringUtils.isEmpty(projectId)) {
            lambdaQueryWrapper.eq(WedprProjectTable::getId, projectId);
        }
        if (!StringUtils.isEmpty(projectName)) {
            lambdaQueryWrapper.like(WedprProjectTable::getName, projectName);
        }
        if (!StringUtils.isEmpty(startTimeStr)) {
            LocalDateTime startTime = Utils.getLocalDateTime(startTimeStr);
            lambdaQueryWrapper.ge(WedprProjectTable::getCreateTime, startTime);
        }
        if (!StringUtils.isEmpty(endTimeStr)) {
            LocalDateTime endTime = Utils.getLocalDateTime(endTimeStr);
            lambdaQueryWrapper.le(WedprProjectTable::getCreateTime, endTime);
        }
        lambdaQueryWrapper.orderByDesc(WedprProjectTable::getLastUpdateTime);
        Page<WedprProjectTable> projectTablePage = new Page<>(pageNum, pageSize);
        Page<WedprProjectTable> page = page(projectTablePage, lambdaQueryWrapper);
        ListProjectResponse listProjectResponse = new ListProjectResponse();
        listProjectResponse.setTotal(page.getTotal());
        listProjectResponse.setProjectList(page.getRecords());
        return listProjectResponse;
    }
}
