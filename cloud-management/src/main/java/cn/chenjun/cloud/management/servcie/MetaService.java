package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.MetaDataEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.MetaMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetaService {
    @Autowired
    private MetaMapper mapper;

    @Autowired
    private GuestMapper guestMapper;

    @Autowired
    private GuestNetworkMapper guestNetworkMapper;

    public String getGuestMetaData(String ip) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("network_ip", ip));
        if (guestNetwork == null) {
            return "";
        }
        List<MetaDataEntity> list = mapper.selectList(new QueryWrapper<MetaDataEntity>().eq("guest_id", guestNetwork.getGuestId()));
        Set<String> metaNames = list.stream().map(MetaDataEntity::getMetaKey).collect(Collectors.toSet());
        return String.join("\r\n", metaNames);
    }

    public String getGuestMetaValue(String ip, String name) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("network_ip", ip));
        if (guestNetwork == null) {
            return name.equals("instance-id") ? UUID.randomUUID().toString() : "";
        }
        MetaDataEntity entity = mapper.selectOne(new QueryWrapper<MetaDataEntity>().eq("guest_id", guestNetwork.getGuestId()).eq("meta_key", name));
        if (entity == null && name.equalsIgnoreCase("instance-id")) {
            GuestEntity guest = this.guestMapper.selectById(guestNetwork.getGuestId());
            return guest == null ? UUID.randomUUID().toString() : guest.getName();
        }
        return entity.getMetaValue();
    }
}
