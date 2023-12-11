package cn.chenjun.cloud.management.data.entity;

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
@TableName("tbl_scheme_info")
public class SchemeEntity {
    public static final String SCHEME_ID = "scheme_id";
    public static final String SCHEME_NAME = "scheme_name";
    public static final String SCHEME_CPU = "scheme_cpu";
    public static final String SCHEME_MEMORY = "scheme_memory";
    public static final String SCHEME_CPU_SPEED = "scheme_cpu_speed";
    public static final String SCHEME_CPU_SOCKETS = "scheme_cpu_sockets";
    public static final String SCHEME_CPU_CORES = "scheme_cpu_cores";
    public static final String SCHEME_CPU_THREADS = "scheme_cpu_threads";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO)
    @TableField(SCHEME_ID)
    private Integer schemeId;
    @TableField(SCHEME_NAME)
    private String name;
    @TableField(SCHEME_CPU)
    private Integer cpu;
    @TableField(SCHEME_MEMORY)
    private Long memory;
    @TableField(SCHEME_CPU_SPEED)
    private Integer speed;
    @TableField(SCHEME_CPU_SOCKETS)
    private Integer sockets;
    @TableField(SCHEME_CPU_CORES)
    private Integer cores;
    @TableField(SCHEME_CPU_THREADS)
    private Integer threads;
    @TableField(CREATE_TIME)
    private Date createTime;
}