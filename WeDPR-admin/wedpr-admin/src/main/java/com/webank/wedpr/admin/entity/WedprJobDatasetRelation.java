package com.webank.wedpr.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

/**
 * @author caryliao
 * @since 2024-09-10
 */
@TableName("wedpr_job_dataset_relation")
@ApiModel(value = "WedprJobDatasetRelation对象", description = "")
public class WedprJobDatasetRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID")
    private String jobId;

    @ApiModelProperty(value = "数据集ID")
    private String datasetId;

    @ApiModelProperty(value = "任务创建时间")
    private String createTime;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "WedprJobDatasetRelation{"
                + "jobId="
                + jobId
                + ", datasetId="
                + datasetId
                + ", createTime="
                + createTime
                + "}";
    }
}
