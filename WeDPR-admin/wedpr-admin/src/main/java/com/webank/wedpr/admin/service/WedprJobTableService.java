package com.webank.wedpr.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.admin.entity.WedprJobTable;
import com.webank.wedpr.admin.request.GetJobDateLineRequest;
import com.webank.wedpr.admin.request.GetWedprJobListRequest;
import com.webank.wedpr.admin.response.GetJobLineResponse;
import com.webank.wedpr.admin.response.GetJobStatisticsResponse;
import com.webank.wedpr.admin.response.ListJobResponse;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-09-04
 */
public interface WedprJobTableService extends IService<WedprJobTable> {
    ListJobResponse listJob(GetWedprJobListRequest getWedprJobListRequest);

    GetJobStatisticsResponse getJobStatistics() throws JsonProcessingException;

    GetJobLineResponse getJobDateLine(GetJobDateLineRequest getJobDateLineRequest)
            throws JsonProcessingException;
}
