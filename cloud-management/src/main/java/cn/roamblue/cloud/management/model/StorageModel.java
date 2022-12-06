package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageModel {
    private int storageId;
    private String name;
    private String type;
    private String param;
    private String mountPath;
    private long capacity;
    private long available;
    private long allocation;
    private int status;
    private Date createTime;
}
