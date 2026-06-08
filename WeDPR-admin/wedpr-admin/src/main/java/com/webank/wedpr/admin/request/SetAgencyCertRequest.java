package com.webank.wedpr.admin.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** Created by caryliao on 2024/8/22 16:58 */
@Data
public class SetAgencyCertRequest {

    @NotBlank
    @Length(max = 64, message = "机构证书id最多64个字符")
    private String certId;

    @NotNull
    @Max(value = 1, message = "certStatus只能为0和1: 0表示启用")
    @Min(value = 0, message = "certStatus只能为0和1: 1表示禁用")
    private Integer certStatus;
}
