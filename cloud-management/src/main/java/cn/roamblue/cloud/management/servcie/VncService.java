package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestVncEntity;
import cn.roamblue.cloud.management.data.mapper.GuestVncMapper;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VncService extends ComponentService{
    @Autowired
    private GuestVncMapper guestVncMapper;

    @Override
    protected int getComponentType() {
        return Constant.ComponentType.VNC;
    }

    @Override
    protected String getComponentName() {
        return "System Vnc";
    }


    public GuestVncEntity getGuestVnc(int guestId){
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

    public void updateVncPort(int guestId,int port){
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if(guestVncEntity!=null){
            guestVncEntity.setPort(port);
            this.guestVncMapper.updateById(guestVncEntity);
        }
    }
}
