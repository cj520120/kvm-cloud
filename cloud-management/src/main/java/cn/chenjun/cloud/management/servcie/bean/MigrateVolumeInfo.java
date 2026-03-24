package cn.chenjun.cloud.management.servcie.bean;

import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrateVolumeInfo {
    private VolumeEntity source;
    private VolumeEntity migrate;
}
