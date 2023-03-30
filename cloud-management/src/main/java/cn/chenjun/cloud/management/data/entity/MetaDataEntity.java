package cn.chenjun.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_meta_data")
public class MetaDataEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("guest_id")
    private Integer guestId;

    @TableField("meta_key")
    private String metaKey;
    @TableField("meta_value")
    private String metaValue;
}
