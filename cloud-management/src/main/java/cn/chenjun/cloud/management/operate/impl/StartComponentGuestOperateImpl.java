package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.component.AbstractComponentService;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class StartComponentGuestOperateImpl extends StartGuestOperateImpl<StartComponentGuestOperate> {

    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private List<AbstractComponentService> componentServices;
    @Autowired
    private ApplicationConfig applicationConfig;

    public StartComponentGuestOperateImpl() {
        super(StartComponentGuestOperate.class);
    }


    @Override
    protected GuestQmaRequest getStartQmaRequest(GuestEntity guest) {
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guest.getGuestId()));
        Optional<AbstractComponentService> componentService = componentServices.stream().filter(t -> Objects.equals(t.getComponentType(), component.getComponentType())).findFirst();
        if (componentService.isPresent()) {
            return componentService.get().getStartQmaRequest(guest.getNetworkId(), guest.getGuestId());
        }
        return null;
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(StartComponentGuestOperate param, ResultUtil<GuestInfo> resultUtil) {
        super.onFinish(param, resultUtil);

    }
}