package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.HostInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.operate.bean.CreateHostOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateHostOperateImpl extends AbstractOperate<CreateHostOperate, ResultUtil<HostInfo>> {

    public CreateHostOperateImpl() {
        super(CreateHostOperate.class);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(CreateHostOperate param) {
        HostEntity host = this.hostMapper.selectById(param.getHostId());
        this.asyncInvoker(host, param, Constant.Command.HOST_INFO, new HashMap<>(0));
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<HostInfo>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(CreateHostOperate param, ResultUtil<HostInfo> resultUtil) {
        HostEntity host = this.hostMapper.selectById(param.getHostId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            host.setStatus(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE);
        } else {
            host.setStatus(cn.roamblue.cloud.management.util.Constant.HostStatus.ERROR);
        }
        this.hostMapper.updateById(host);
    }
}
