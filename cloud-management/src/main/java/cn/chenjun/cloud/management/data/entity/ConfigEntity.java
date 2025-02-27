package cn.chenjun.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_system_config")
public class ConfigEntity {
    public static final String CONFIG_ID = "config_id";
    public static final String CONFIG_KEY = "config_key";
    public static final String CONFIG_ALLOCATE_TYPE = "config_allocate_type";
    public static final String CONFIG_ALLOCATE_ID = "config_allocate_id";
    public static final String CONFIG_VALUE = "config_value";

    @TableId(type = IdType.AUTO, value = CONFIG_ID)
    private Integer id;
    @TableField(CONFIG_KEY)
    private String configKey;
    @TableField(CONFIG_ALLOCATE_TYPE)
    private Integer allocateType;
    @TableField(CONFIG_ALLOCATE_ID)
    private Integer allocateId;
    @TableField(CONFIG_VALUE)
    private String configValue;
}