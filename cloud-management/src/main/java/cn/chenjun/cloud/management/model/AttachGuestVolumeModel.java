package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachGuestVolumeModel {
    private GuestModel guest;
    private VolumeModel volume;
}
