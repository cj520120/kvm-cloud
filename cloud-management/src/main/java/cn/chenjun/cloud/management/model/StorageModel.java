package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageModel extends SimpleStorageModel {
    private HostModel host;
}
