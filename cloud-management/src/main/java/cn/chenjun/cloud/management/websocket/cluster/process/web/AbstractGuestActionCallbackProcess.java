package cn.chenjun.cloud.management.websocket.cluster.process.web;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.websocket.cluster.process.AbstractClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.manager.WebClientManager;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractGuestActionCallbackProcess extends AbstractClusterMessageProcess<ResultUtil<GuestEntity>> {



    @Override
    protected void doProcess(NotifyData<ResultUtil<GuestEntity>> msg) {
        ResultUtil<GuestEntity> resultUtil = msg.getData();
        GuestEntity guest = resultUtil.getData();
        GuestDetail guestDetail = GuestDetail.builder().id(guest.getGuestId()).name(guest.getName()).ip(guest.getGuestIp()).description(guest.getDescription()).build();
        ResultUtil<GuestDetail> result = ResultUtil.<GuestDetail>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).data(guestDetail).build();
        NotifyData<ResultUtil<GuestDetail>> sendMsg = NotifyData.<ResultUtil<GuestDetail>>builder().id(msg.getId()).type(this.getNotifyType()).data(result).version(System.currentTimeMillis()).build();
        WebClientManager.sendAll(sendMsg);
    }


    protected abstract int getNotifyType();

    @Data
    @Builder
    public static class GuestDetail {
        private int id;
        private String name;
        private String ip;
        private String description;
    }

}
