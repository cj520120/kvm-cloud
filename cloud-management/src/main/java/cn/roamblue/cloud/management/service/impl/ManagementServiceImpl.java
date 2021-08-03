package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.management.data.entity.ManagementEntity;
import cn.roamblue.cloud.management.data.entity.ManagementTaskEntity;
import cn.roamblue.cloud.management.data.mapper.ManagementMapper;
import cn.roamblue.cloud.management.data.mapper.ManagementTaskMapper;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.service.ManagementService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.ServiceId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class ManagementServiceImpl extends AbstractService implements ManagementService {
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
        ManagementEntity entity = managementMapper.findByServerId(ServiceId.CURRENT_SERVICE_ID);
        if (entity == null) {
            entity = ManagementEntity.builder().serverId(ServiceId.CURRENT_SERVICE_ID).lastActiveTime(new Date()).createTime(new Date()).build();
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
                    taskEntity = ManagementTaskEntity.builder().taskName(name).serverId(ServiceId.CURRENT_SERVICE_ID).createTime(new Date()).build();
                    managementTaskMapper.insert(taskEntity);
                    return true;
                } else {
                    QueryWrapper<ManagementEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("server_id", taskEntity.getServerId());
                    ManagementEntity entity = managementMapper.selectOne(queryWrapper);
                    if (entity == null || taskEntity.getServerId().equals(ServiceId.CURRENT_SERVICE_ID) || System.currentTimeMillis() - entity.getLastActiveTime().getTime() > 30000) {
                        //请求更新
                        QueryWrapper<ManagementTaskEntity> wrapper = new QueryWrapper<>();
                        wrapper.eq("server_id", taskEntity.getServerId());
                        wrapper.eq("task_name", taskEntity.getTaskName());
                        wrapper.eq("create_time", taskEntity.getCreateTime());
                        taskEntity = ManagementTaskEntity.builder().taskName(name).serverId(ServiceId.CURRENT_SERVICE_ID).createTime(new Date()).build();
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

