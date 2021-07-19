package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 系统类型
 *
 * @author chenjun
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OsCategoryInfo {

    /**
     * ID
     */
    private int id;
    /**
     * 名称
     */
    private String categoryName;
    /**
     * 网卡驱动
     */
    private String networkDriver;
    /**
     * 磁盘驱动
     */
    private String diskDriver;
    /**
     * 创建时间
     */
    private Date createTime;
}
