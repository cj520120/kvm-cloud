package cn.roamblue.cloud.management.v2.data.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("tbl_volume_info")
public class VolumeEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    private int parentId;
    private int clusterId;
    private int storageId;
    private String name;
    private String target;
    private long capacity;
    private long allocation;
    private String type;
    private int status;

}
