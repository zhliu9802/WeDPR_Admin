package com.webank.wedpr.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.webank.wedpr.admin.entity.WedprJobDatasetRelation;
import com.webank.wedpr.admin.entity.WedprJobTable;
import com.webank.wedpr.admin.mapper.WedprJobDatasetRelationMapper;
import com.webank.wedpr.admin.request.GetJobByDatasetRequest;
import com.webank.wedpr.admin.response.ListJobResponse;
import com.webank.wedpr.admin.service.WedprJobDatasetRelationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-09-06
 */
@Service
public class WedprJobDatasetRelationServiceImpl
        extends ServiceImpl<WedprJobDatasetRelationMapper, WedprJobDatasetRelation>
        implements WedprJobDatasetRelationService {
    @Autowired private WedprJobDatasetRelationMapper wedprJobDatasetRelationMapper;

    @Override
    public ListJobResponse queryJobsByDatasetId(GetJobByDatasetRequest getJobByDatasetRequest) {
        String datasetId = getJobByDatasetRequest.getDatasetId();
        Integer pageNum = getJobByDatasetRequest.getPageNum();
        Integer pageSize = getJobByDatasetRequest.getPageSize();
        try (Page<Object> objectPage = PageMethod.startPage(pageNum, pageSize)) {
            List<WedprJobTable> wedprJobTableList =
                    wedprJobDatasetRelationMapper.queryJobsByDatasetId(datasetId);
            long total = new PageInfo<>(wedprJobTableList).getTotal();
            ListJobResponse listJobResponse = new ListJobResponse();
            listJobResponse.setTotal(total);
            listJobResponse.setJobList(wedprJobTableList);
            return listJobResponse;
        }
    }
}
