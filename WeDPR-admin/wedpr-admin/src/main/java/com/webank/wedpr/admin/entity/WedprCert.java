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
@TableName("wedpr_cert")
@ApiModel(value = "WedprCert对象", description = "")
@Data
public class WedprCert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("cert_id")
    @ApiModelProperty(value = "证书id")
    private String certId;

    @ApiModelProperty(value = "机构编号")
    private String agencyId;

    @ApiModelProperty(value = "机构名")
    private String agencyName;

    @ApiModelProperty(value = "机构证书请求文件名称")
    private String csrFileName;

    @ApiModelProperty(value = "机构证书请求文件内容")
    private String csrFileText;

    @ApiModelProperty(value = "机构证书文件内容")
    private String certFileText;

    @ApiModelProperty(value = "过期时间")
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "证书状态(0：无证书，1：有效，2：过期，3：禁用)")
    private Integer certStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;
}
