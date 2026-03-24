package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.management.data.entity.TaskEntity;
import cn.chenjun.cloud.management.data.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class TaskDao {
    @Autowired
    private TaskMapper mapper;

    public TaskEntity findById(String id) {
        return mapper.selectById(id);
    }

    public List<TaskEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public List<TaskEntity> listCanRunTask(int count) {
        QueryWrapper<TaskEntity> wrapper = new QueryWrapper<TaskEntity>().lt(TaskEntity.EXPIRE_TIME, new Date(System.currentTimeMillis()));
        wrapper.last("limit 0," + count);
        return this.mapper.selectList(wrapper);
    }

    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    public void update(TaskEntity entity) {
        mapper.updateById(entity);
    }

    public void keep(String taskId, Date expireTime) {
        mapper.keep(taskId, expireTime);
    }

    public int updateVersion(String taskId, int oldVersion, Date expireTime) {
        return mapper.updateVersion(taskId, oldVersion, expireTime);
    }

    public TaskEntity insert(TaskEntity entity) {
        mapper.insert(entity);
        return entity;
    }
}
