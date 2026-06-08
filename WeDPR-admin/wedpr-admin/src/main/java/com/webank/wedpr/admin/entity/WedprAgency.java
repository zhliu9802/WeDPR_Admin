package com.webank.wedpr.admin.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author caryliao
 * @since 2024-08-22
 */
@TableName("wedpr_agency")
@ApiModel(value = "WedprAgency对象", description = "")
@Data
public class WedprAgency implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("agency_id")
    @ApiModelProperty(value = "机构编号")
    private String agencyId;

    @ApiModelProperty(value = "机构名")
    private String agencyName;

    @ApiModelProperty(value = "机构描述")
    private String agencyDesc;

    @ApiModelProperty(value = "机构联系人")
    private String agencyContact;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "网关地址")
    private String gatewayEndpoint;

    @ApiModelProperty(value = "机构状态(0:启用，1:禁用)")
    private Integer agencyStatus;

    @ApiModelProperty(value = "机构用户数")
    private Integer userCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;
}
