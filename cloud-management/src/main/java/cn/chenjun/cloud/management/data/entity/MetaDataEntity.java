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
@TableName("tbl_meta_data")
public class MetaDataEntity {
    public static final String ID = "id";
    public static final String GUEST_ID = "guest_id";
    public static final String META_KEY = "meta_key";
    public static final String META_VALUE = "meta_value";

    @TableId(type = IdType.AUTO,value = ID)
    private Integer id;
    @TableField(GUEST_ID)
    private Integer guestId;
    @TableField(META_KEY)
    private String metaKey;
    @TableField(META_VALUE)
    private String metaValue;
}