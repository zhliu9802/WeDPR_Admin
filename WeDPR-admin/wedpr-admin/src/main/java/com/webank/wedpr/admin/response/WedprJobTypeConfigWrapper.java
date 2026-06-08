package com.webank.wedpr.admin.response;

import java.util.List;
import lombok.Data;

@Data
public class WedprJobTypeConfigWrapper {
    private String version;
    private List<JobTypeConfig> templates;
}
