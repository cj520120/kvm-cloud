package com.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VmStatisticsInfo {
    private Date time;
    private float read;
    private float write;
    private float send;
    private float receive;
    private float cpu;
}
