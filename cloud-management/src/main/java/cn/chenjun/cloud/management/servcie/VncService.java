package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestVncEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestVncMapper;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private NotifyService notifyService;

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

    public void updateVncPort(int guestId, int port) {
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if (guestVncEntity != null) {
            guestVncEntity.setPort(port);
            this.guestVncMapper.updateById(guestVncEntity);
        }
    }


}
