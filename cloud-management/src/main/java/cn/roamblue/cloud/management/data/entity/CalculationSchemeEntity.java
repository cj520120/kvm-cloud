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

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_calculation_scheme")
public class CalculationSchemeEntity {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("scheme_name")
    private String schemeName;
    @TableField("scheme_cpu")
    private Integer schemeCpu;
    @TableField("scheme_memory")
    private Long schemeMemory;
    @TableField("scheme_cpu_speed")
    private Integer schemeCpuSpeed;

    @TableField("scheme_cpu_socket")
    private Integer schemeCpuSocket;
    @TableField("scheme_cpu_core")
    private Integer schemeCpuCore;
    @TableField("scheme_cpu_threads")
    private Integer schemeCpuThreads;
    @TableField("create_time")
    private Date createTime;
}
