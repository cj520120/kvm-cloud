package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleVolumeModel {

    private Integer volumeId;
    private String description;
    private int templateId;
    private int storageId;
    private int hostId;
    private String name;
    private String path;
    private long capacity;
    private long allocation;
    private String type;
    private int status;

    private int guestId;
    private int deviceId;
    private String deviceDriver;
    private Date createTime;
    private SimpleGuestModel guest;

}
