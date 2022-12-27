package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.component.VncService;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.VncUpdateOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author chenjun
 */
@Component
@Slf4j
public class VncUpdateOperateImpl extends AbstractOperate<VncUpdateOperate, ResultUtil<Void>> {

    @Autowired
    private VncService vncService;

    public VncUpdateOperateImpl() {
        super(VncUpdateOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(VncUpdateOperate param) {
        GuestEntity vncGuest = this.vncService.getGuestVncServer(param.getGuestId());
        HostEntity host = this.hostMapper.selectById(vncGuest.getHostId());
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName(vncGuest.getName());
        request.setTimeout((int) TimeUnit.SECONDS.toSeconds(30));
        request.setCommands(commands);
        String file = "/usr/local/websockify/token/vnc_" + param.getGuestId();
        String body = String.format("%s: %s:%d", param.getToken(), param.getIp(), param.getPort());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName(file).fileBody(body).build())).build());
        this.asyncInvoker(host, param, Constant.Command.GUEST_QMA, request);
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

}