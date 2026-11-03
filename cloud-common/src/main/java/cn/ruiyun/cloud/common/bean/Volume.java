package cn.ruiyun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Volume {
    private String storage;
    private String name;
    private String path;
    private String type;
    private long capacity;
    private List<Config> configs;
}
