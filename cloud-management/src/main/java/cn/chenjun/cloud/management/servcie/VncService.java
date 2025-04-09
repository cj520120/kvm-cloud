package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestVncEntity;
import cn.chenjun.cloud.management.data.mapper.GuestVncMapper;
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
    private static final int VNC_PASSWORD_SIZE = 8;
    private static final int VNC_TOKEN_SIZE = 16;

    public GuestVncEntity getGuestVnc(int guestId) {
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if (guestVncEntity == null) {
            guestVncEntity = GuestVncEntity.builder()
                    .guestId(guestId)
                    .port(0)
                    .password(RandomStringUtils.randomAlphanumeric(VNC_PASSWORD_SIZE))
                    .token(RandomStringUtils.randomAlphanumeric(VNC_TOKEN_SIZE))
                    .build();
            this.guestVncMapper.insert(guestVncEntity);
        } else {
            if (guestVncEntity.getPassword().length() > VNC_PASSWORD_SIZE) {
                guestVncEntity.setPassword(RandomStringUtils.randomAlphanumeric(VNC_PASSWORD_SIZE));
                this.guestVncMapper.updateById(guestVncEntity);
            }
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
