package cn.ruiyun.cloud.management.servcie.bean;

import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.data.entity.GuestNetworkEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachNicInfo {
    private GuestEntity guest;
    private GuestNetworkEntity nic;
}
