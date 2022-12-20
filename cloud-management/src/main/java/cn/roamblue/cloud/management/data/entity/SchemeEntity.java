package cn.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_scheme_info")
public class SchemeEntity {
    @TableId(type = IdType.AUTO)
    @TableField("scheme_id")
    private Integer schemeId;
    @TableField("scheme_name")
    private String name;
    @TableField("scheme_cpu")
    private Integer cpu;
    @TableField("scheme_memory")
    private Long memory;
    @TableField("scheme_cpu_speed")
    private Integer speed;
    @TableField("scheme_cpu_sockets")
    private Integer sockets;
    @TableField("scheme_cpu_cores")
    private Integer cores;
    @TableField("scheme_cpu_threads")
    private Integer threads;
    @TableField("create_time")
    private Date createTime;
}
