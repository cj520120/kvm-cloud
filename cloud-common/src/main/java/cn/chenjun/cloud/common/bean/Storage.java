package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Storage {
    /**
     * 存储池名称
     */
    private String name;
    /**
     * 存储池类型
     */
    private String type;
    /**
     * 挂载路径
     */
    private String mountPath;
    /**
     * 参数
     */
    private Map<String, Object> param;
}
