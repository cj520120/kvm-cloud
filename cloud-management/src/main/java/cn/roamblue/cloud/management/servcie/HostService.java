package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.AppUtils;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.model.HostModel;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.CreateHostOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HostService {
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private OperateTask operateTask;
    private HostModel initHost(HostEntity entity){
        return new BeanConverter<>(HostModel.class).convert(entity,null);
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<HostModel>> listAllHost(){
        List<HostEntity> hostList=this.hostMapper.selectList(new QueryWrapper<>());
        List<HostModel> models=hostList.stream().map(this::initHost).collect(Collectors.toList());
        return ResultUtil.success(models);
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> getHostInfo(int hostId){
        HostEntity host=this.hostMapper.selectById(hostId);
        if(host==null){
            throw new CodeException(ErrorCode.HOST_NOT_FOUND,"主机不存在");
        }
        return ResultUtil.success(this.initHost(host));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> createHost(String name,String ip ,String uri,String nic){
        String clientId= AppUtils.getAppId();
        String clientSecret=AppUtils.getAppSecret(clientId);
        HostEntity host= HostEntity.builder().hostIp(ip).displayName(name)
                .uri(uri).arch("").emulator("").nic(nic).hypervisor("")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .totalMemory(0L)
                .allocationMemory(0L)
                .totalCpu(0)
                .allocationCpu(0)
                .sockets(0)
                .threads(0)
                .cores(0)
                .status(Constant.HostStatus.REGISTER).build();
        this.hostMapper.insert(host);
        BaseOperateParam operateParam= CreateHostOperate.builder().hostId(host.getHostId()).taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initHost(host));
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> registerHost(int hostId){
        HostEntity host=this.hostMapper.selectById(hostId);
        host.setStatus(Constant.HostStatus.REGISTER);
        if(StringUtils.isEmpty(host.getClientId())){
            host.setClientId(AppUtils.getAppId());
            host.setClientSecret(AppUtils.getAppSecret(host.getClientId()));
        }
        this.hostMapper.updateById(host);
        BaseOperateParam operateParam= CreateHostOperate.builder().hostId(host.getHostId()).taskId(UUID.randomUUID().toString()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initHost(host));
    }
}
