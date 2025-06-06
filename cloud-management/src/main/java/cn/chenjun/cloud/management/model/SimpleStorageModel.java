package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleStorageModel {
    private int storageId;
    private String description;
    private String name;
    private String type;
    private String param;
    private String mountPath;
    private int hostId;
    private int supportCategory;
    private long capacity;
    private long available;
    private long allocation;
    private int status;
    private Date createTime;
}
