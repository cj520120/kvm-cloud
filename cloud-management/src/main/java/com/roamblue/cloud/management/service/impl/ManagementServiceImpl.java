package com.roamblue.cloud.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.management.data.entity.ManagementEntity;
import com.roamblue.cloud.management.data.entity.ManagementTaskEntity;
import com.roamblue.cloud.management.data.mapper.ManagementMapper;
import com.roamblue.cloud.management.data.mapper.ManagementTaskMapper;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.service.ManagementService;
import com.roamblue.cloud.management.util.LockKeyUtil;
import com.roamblue.cloud.management.util.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ManagementServiceImpl implements ManagementService {
    private ManagementMapper managementMapper;
    @Autowired
    private LockService lockService;

    @Autowired
    private ManagementTaskMapper managementTaskMapper;

    public ManagementServiceImpl(ManagementMapper managementMapper) {
        this.managementMapper = managementMapper;
        this.keep();
    }


    @Override
    public void keep() {
        ManagementEntity entity = managementMapper.findByServerId(ServiceUtil.SERVICE_ID);
        if (entity == null) {
            entity = ManagementEntity.builder().serverId(ServiceUtil.SERVICE_ID).lastActiveTime(new Date()).createTime(new Date()).build();
            managementMapper.insert(entity);
        } else {
            entity.setLastActiveTime(new Date());
            managementMapper.updateById(entity);
        }
    }

    @Override
    public boolean applyTask(String name) {
        if (lockService.tryLock(LockKeyUtil.getTaskLock(name), 10, TimeUnit.SECONDS)) {
            try {
                ManagementTaskEntity taskEntity = managementTaskMapper.selectById(name);
                if (taskEntity == null) {
                    taskEntity = ManagementTaskEntity.builder().taskName(name).serverId(ServiceUtil.SERVICE_ID).createTime(new Date()).build();
                    managementTaskMapper.insert(taskEntity);
                    return true;
                } else {
                    QueryWrapper<ManagementEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("server_id", taskEntity.getServerId());
                    ManagementEntity entity = managementMapper.selectOne(queryWrapper);
                    if (entity == null || taskEntity.getServerId().equals(ServiceUtil.SERVICE_ID) || System.currentTimeMillis() - entity.getLastActiveTime().getTime() > 30000) {
                        //请求更新
                        QueryWrapper<ManagementTaskEntity> wrapper = new QueryWrapper<>();
                        wrapper.eq("server_id", taskEntity.getServerId());
                        wrapper.eq("task_name", taskEntity.getTaskName());
                        wrapper.eq("create_time", taskEntity.getCreateTime());
                        taskEntity = ManagementTaskEntity.builder().taskName(name).serverId(ServiceUtil.SERVICE_ID).createTime(new Date()).build();
                        return managementTaskMapper.update(taskEntity, wrapper) > 0;

                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            } finally {
                lockService.unLock(LockKeyUtil.getTaskLock(name));
            }
        } else {
            return false;
        }
    }
}

