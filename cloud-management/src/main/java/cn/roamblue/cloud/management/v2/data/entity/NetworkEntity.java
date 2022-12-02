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
@TableName("tbl_network_info")
public class NetworkEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    private Integer clusterId;
    private String name;
    private String startIp;
    private String endIp;
    private String gateway;
    private String mask;
    private String bridge;
    private String dns;
    private String type;
    private int status;
    private int vlanId;
    private int parentId;
}
