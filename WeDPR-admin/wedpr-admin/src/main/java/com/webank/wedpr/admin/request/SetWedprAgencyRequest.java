package com.webank.wedpr.admin.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** Created by caryliao on 2024/8/22 16:58 */
@Data
public class SetWedprAgencyRequest {

    @NotBlank
    @Length(max = 64, message = "机构id最多64个字符")
    private String agencyId;

    @NotNull
    @Max(value = 1, message = "agencyStatus只能为0和1: 0表示启用")
    @Min(value = 0, message = "agencyStatus只能为0和1: 1表示禁用")
    private Integer agencyStatus;
}
