package cn.roamblue.cloud.common.agent;

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
public class StorageRequest {
    /**
     * 命令
     */
    private String command;
    /**
     * 存储池名称
     */
    private String name;
    /**
     * 存储池类型
     */
    private String type;

    /**
     * 参数
     */
    private Map<String, Object> param;

}
