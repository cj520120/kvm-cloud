package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestModel {
    private int guestId;
    private String name;
    private String description;
    private String busType;
    private int cpu;
    private long memory;
    private int speed;
    private int cdRoom;
    private int hostId;
    private int schemeId;
    private int lastHostId;
    private int type;
    private int networkId;
    private int status;
    private String guestIp;
    private Date lastStartTime;
    private Date createTime;
}
