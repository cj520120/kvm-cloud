package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterModel {
    private int clusterId;
    private String clusterName;
    private String appId;
    private String appSecret;
    private Date createTime;
}
