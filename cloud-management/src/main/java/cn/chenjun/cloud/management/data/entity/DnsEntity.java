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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_dns_info")
public class DnsEntity {
    @TableId(type = IdType.AUTO)
    @TableField("dns_id")
    private Integer dnsId;
    @TableField("network_id")
    private Integer networkId;
    @TableField("dns_domain")
    private String dnsDomain;
    @TableField("dns_ip")
    private String dnsIp;
    @TableField("create_time")
    private Date createTime;

}
