package com.webank.wedpr.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.admin.entity.WedprJobDatasetRelation;
import com.webank.wedpr.admin.request.GetJobByDatasetRequest;
import com.webank.wedpr.admin.response.ListJobResponse;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-09-10
 */
public interface WedprJobDatasetRelationService extends IService<WedprJobDatasetRelation> {

    ListJobResponse queryJobsByDatasetId(GetJobByDatasetRequest getJobByDatasetRequest);
}
