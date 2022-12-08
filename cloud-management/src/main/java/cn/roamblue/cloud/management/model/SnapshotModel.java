package cn.roamblue.cloud.management.model;

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
public class SnapshotModel {

    private int snapshotVolumeId;
    private String name;
    private int storageId;
    private String volumeName;
    private String volumePath;
    private long capacity;
    private long allocation;
    private String type;
    private int status;
    private Date createTime;

}
