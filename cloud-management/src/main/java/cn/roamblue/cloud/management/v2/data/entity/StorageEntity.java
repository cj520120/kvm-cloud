package cn.roamblue.cloud.management.v2.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_storage_info")
public class StorageEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    private int clusterId;
    private String name;
    private String type;
    private String param;
    /**
     * 容量
     */
    private long capacity;
    /**
     * 可用
     */
    private long available;
    /**
     * 已用
     */
    private long allocation;
    private int status;
    private Date createTime;
}
