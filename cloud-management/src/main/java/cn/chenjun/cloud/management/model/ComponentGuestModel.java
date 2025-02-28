package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ComponentGuestModel extends GuestModel {
    private int componentId;
    private int componentType;
    private String componentVip;
    private String basicComponentVip;
}
