package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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
    private int cdRoom;
    private int hostId;
    private int lastHostId;
    private int type;
    private int status;
    private List<VolumeModel> volumes;
    private List<GuestNetworkModel> networks;
    private Date createTime;
}
