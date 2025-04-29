package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SimpleGuestModel {
    private int guestId;
    private String name;
    private String description;
    private int cpu;
    private long memory;
    private int share;
    private int cdRoom;
    private int hostId;
    private int schemeId;
    private int lastHostId;
    private int type;
    private int networkId;
    private int groupId;
    private int status;
    private int systemCategory;
    private int bootstrapType;
    private String guestIp;
    private Date lastStartTime;
    private Date createTime;
}
