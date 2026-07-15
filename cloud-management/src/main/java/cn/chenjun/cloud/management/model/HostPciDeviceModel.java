package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HostPciDeviceModel {
    private int id;
    private int guestId;
    private int hostId;
    private String domain;
    private String bus;
    private String slot;
    private String func;
    private String description;
    private Date createTime;
}
