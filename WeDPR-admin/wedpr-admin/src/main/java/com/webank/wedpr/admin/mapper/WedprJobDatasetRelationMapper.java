package com.webank.wedpr.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedpr.admin.entity.WedprJobDatasetRelation;
import com.webank.wedpr.admin.entity.WedprJobTable;
import java.util.List;

/**
 * Mapper 接口
 *
 * @author caryliao
 * @since 2024-09-06
 */
public interface WedprJobDatasetRelationMapper extends BaseMapper<WedprJobDatasetRelation> {

    List<WedprJobTable> queryJobsByDatasetId(String datasetId);
}
