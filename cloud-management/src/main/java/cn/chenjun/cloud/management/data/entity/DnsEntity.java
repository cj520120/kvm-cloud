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
@TableName("tbl_dns_info")
public class DnsEntity {
    public static final String DNS_ID = "dns_id";
    public static final String NETWORK_ID = "network_id";
    public static final String DNS_DOMAIN = "dns_domain";
    public static final String DNS_IP = "dns_ip";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = DNS_ID)
    private Integer dnsId;
    @TableField(NETWORK_ID)
    private Integer networkId;
    @TableField(DNS_DOMAIN)
    private String dnsDomain;
    @TableField(DNS_IP)
    private String dnsIp;
    @TableField(CREATE_TIME)
    private Date createTime;

}