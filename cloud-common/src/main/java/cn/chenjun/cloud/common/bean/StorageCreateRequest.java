package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageCreateRequest {
    /**
     * 存储池名称
     */
    private String name;
    private String secretXml;
    private String storageXml;
    private String secretValue;

}
