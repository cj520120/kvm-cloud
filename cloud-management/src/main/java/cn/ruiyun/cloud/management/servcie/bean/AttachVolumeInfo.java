package cn.ruiyun.cloud.management.servcie.bean;

import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.data.entity.VolumeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachVolumeInfo {
    private GuestEntity guest;
    private VolumeEntity volume;
}
