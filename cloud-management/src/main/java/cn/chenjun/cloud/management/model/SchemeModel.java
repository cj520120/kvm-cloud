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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemeModel {
    private int schemeId;
    private String name;
    private int cpu;
    private long memory;
    private int share;
    private int sockets;
    private int cores;
    private int threads;
    private Date createTime;
}
