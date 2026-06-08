package com.webank.wedpr.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

/**
 * @author caryliao
 * @since 2024-09-04
 */
@TableName("wedpr_project_table")
@ApiModel(value = "WedprProjectTable对象", description = "")
public class WedprProjectTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "项目ID")
    @TableId("id")
    private String id;

    @ApiModelProperty(value = "项目名称")
    private String name;

    @ApiModelProperty(value = "项目描述")
    @TableField("`desc`")
    private String projectDesc;

    @ApiModelProperty(value = "项目属主")
    private String owner;

    @ApiModelProperty(value = "项目所属机构")
    private String ownerAgency;

    @ApiModelProperty(value = "项目类型(Export/Wizard)")
    private String projectType;

    @ApiModelProperty(value = "项目标签")
    private String label;

    @ApiModelProperty(value = "上报状态")
    private Integer reportStatus;

    @ApiModelProperty(value = "项目创建时间")
    private String createTime;

    @ApiModelProperty(value = "项目更新时间")
    private String lastUpdateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerAgency() {
        return ownerAgency;
    }

    public void setOwnerAgency(String ownerAgency) {
        this.ownerAgency = ownerAgency;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "WedprProjectTable{"
                + "id="
                + id
                + ", name="
                + name
                + ", desc="
                + projectDesc
                + ", owner="
                + owner
                + ", ownerAgency="
                + ownerAgency
                + ", projectType="
                + projectType
                + ", label="
                + label
                + ", reportStatus="
                + reportStatus
                + ", createTime="
                + createTime
                + ", lastUpdateTime="
                + lastUpdateTime
                + "}";
    }
}
