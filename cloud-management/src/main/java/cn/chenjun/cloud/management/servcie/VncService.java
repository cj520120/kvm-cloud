package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestVncEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestVncMapper;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.model.VncModel;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class VncService {
    @Autowired
    protected GuestVncMapper guestVncMapper;
    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private EventService eventService;

    public GuestVncEntity getGuestVnc(int guestId) {
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if (guestVncEntity == null) {
            guestVncEntity = GuestVncEntity.builder()
                    .guestId(guestId)
                    .port(0)
                    .password(RandomStringUtils.randomAlphanumeric(8))
                    .token(RandomStringUtils.randomAlphanumeric(16))
                    .build();
            this.guestVncMapper.insert(guestVncEntity);
        }
        return guestVncEntity;
    }

    public void updateVncPort(int networkId, int guestId, int port) {
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if (guestVncEntity != null) {
            guestVncEntity.setPort(port);
            this.guestVncMapper.updateById(guestVncEntity);
        }
        this.eventService.publish(NotifyData.<List<VncModel>>builder().id(networkId).type(Constant.NotifyType.COMPONENT_UPDATE_VNC).data(this.listVncByNetworkId(networkId)).build());
    }

    public List<VncModel> listVncByNetworkId(int networkId) {
        List<GuestVncEntity> guestVncList = this.guestVncMapper.selectList(new QueryWrapper<>()).stream().filter(guestVncEntity -> guestVncEntity.getPort() > 0 && !StringUtils.isEmpty(guestVncEntity.getToken())).collect(Collectors.toList());
        List<Integer> guestIds = guestVncList.stream().map(GuestVncEntity::getGuestId).collect(Collectors.toList());
        if (guestIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<GuestEntity> guestList = this.guestMapper.selectBatchIds(guestIds).stream().filter(guestEntity -> guestEntity.getNetworkId() == networkId && guestEntity.getHostId() > 0).collect(Collectors.toList());
        List<Integer> hostIds = guestList.stream().map(GuestEntity::getHostId).collect(Collectors.toList());
        if (hostIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<HostEntity> hostList = this.hostMapper.selectBatchIds(hostIds);

        Map<Integer, String> hostIpMap = hostList.stream().collect(Collectors.toMap(HostEntity::getHostId, HostEntity::getHostIp));
        Map<Integer, Integer> guestHostIdMap = guestList.stream().collect(Collectors.toMap(GuestEntity::getGuestId, GuestEntity::getHostId));

        return guestVncList.stream().map(guestVncEntity -> {
            int hostId = guestHostIdMap.getOrDefault(guestVncEntity.getGuestId(), 0);
            String hostIp = hostIpMap.getOrDefault(hostId, "");
            if (ObjectUtils.isEmpty(hostIp)) {
                return null;
            }
            return VncModel.builder().host(hostIp).port(guestVncEntity.getPort()).token(guestVncEntity.getToken()).build();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
