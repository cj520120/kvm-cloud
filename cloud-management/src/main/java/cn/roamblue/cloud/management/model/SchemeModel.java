package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemeModel {
    private int schemeId;
    private String name;
    private int cpu;
    private long memory;
    private int speed;
    private int sockets;
    private int cores;
    private int threads;
    private Date createTime;
}
