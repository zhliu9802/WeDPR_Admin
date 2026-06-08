package com.webank.wedpr.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedpr.admin.entity.WedprJobTable;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper 接口
 *
 * @author caryliao
 * @since 2024-09-04
 */
public interface WedprJobTableMapper extends BaseMapper<WedprJobTable> {
    List<WedprJobTable> jobTypeStatistic();

    WedprJobTable jobAgencyStatistic(@Param("agencyName") String agencyName);

    WedprJobTable jobAgencyTypeStatistic(
            @Param("agencyName") String agencyName, @Param("jobType") String jobType);

    List<WedprJobTable> getJobDateLine(
            @Param("jobType") String jobType,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
}
