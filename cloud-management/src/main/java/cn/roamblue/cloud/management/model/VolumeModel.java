package cn.roamblue.cloud.management.model;

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
public class VolumeModel {

    private Integer volumeId;
    private String description;
    private int templateId;
    private int storageId;
    private String name;
    private String path;
    private long capacity;
    private long allocation;
    private String type;
    private int status;
    private VolumeAttachModel attach;
    private Date createTime;

}
