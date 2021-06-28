package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 模版下载信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateRefInfo implements Serializable {

    /**
     * id
     */
    private int id;
    /**
     * 存储ID
     */
    private int storageId;
    /**
     * 集群ID
     */
    private int clusterId;
    /**
     * 模版ID
     */
    private int templateId;
    /**
     * 模版文件
     */
    private String target;
    /**
     * 模版状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Date createTime;


}
