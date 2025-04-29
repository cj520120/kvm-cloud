package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class VolumeModel extends SimpleVolumeModel {
    private SimpleStorageModel storage;
    private HostModel host;
    private TemplateModel template;

}
