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
@TableName("tbl_cluster_info")
public class ClusterEntity {
    @TableId(type = IdType.AUTO)
    @TableField("cluster_id")
    private int clusterId;
    @TableField("cluster_name")
    private String clusterName;
    @TableField("app_id")
    private String appId;
    @TableField("app_secret")
    private String appSecret;
    @TableField("create_time")
    private Date createTime;

}
