package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateModel {
    private int templateId;
    private String name;
    private String uri;
    private String arch;
    private String md5;
    private int templateType;
    private String vendorData;
    private String localCloudCfg;
    private int cloudWaitFlag;
    private int status;
    private Date createTime;
}
