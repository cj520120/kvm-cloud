package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 模版信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateInfo implements Serializable {
    /**
     * id
     */
    private int id;
    /**
     * 模版名称
     */
    private String name;
    /**
     * 集群ID
     */
    private int clusterId;
    /**
     * 远程地址
     */
    private String uri;
    /**
     * 模版类型
     */
    private String type;
    /**
     * 系统类型ID
     */
    private int osCategoryId;
    /**
     * 模版状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Date createTime;

}
