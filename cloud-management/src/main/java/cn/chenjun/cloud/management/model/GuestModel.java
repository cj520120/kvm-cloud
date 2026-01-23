package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GuestModel extends SimpleGuestModel {
    private HostModel host;
    private HostModel bindHost;
    private GroupModel group;
    private SchemeModel scheme;
    private NetworkModel network;
    private TemplateModel template;
    private ComponentGuestModel component;
}
