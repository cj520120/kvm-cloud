package com.roamblue.cloud.common.agent;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class VmStaticsModel implements Serializable {
    private String name;
    private long time;
    private long diskReadSpeed;
    private long diskWriteSpeed;
    private long networkSendSpeed;
    private long networkReceiveSpeed;
    private int cpuUsage;
}
