package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.management.data.entity.ConfigEntity;
import cn.chenjun.cloud.management.data.mapper.ConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConfigDao {
    @Autowired
    private ConfigMapper mapper;

    public ConfigEntity findById(int id) {
        return mapper.selectById(id);
    }

    public ConfigEntity findByAllocateKey(String key, int type, int id) {
        QueryWrapper<ConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ConfigEntity.CONFIG_KEY, key).eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, type).eq(ConfigEntity.CONFIG_ALLOCATE_ID, id);
        return mapper.selectOne(queryWrapper);
    }

    public List<ConfigEntity> listByAllocate(int type, int id) {
        QueryWrapper<ConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, type).eq(ConfigEntity.CONFIG_ALLOCATE_ID, id);
        return mapper.selectList(queryWrapper);
    }

    public List<ConfigEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void deleteByAllocate(int type, int id) {
        QueryWrapper<ConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ConfigEntity.CONFIG_ALLOCATE_TYPE, type).eq(ConfigEntity.CONFIG_ALLOCATE_ID, id);
        mapper.delete(queryWrapper);
    }

    public void update(ConfigEntity entity) {
        mapper.updateById(entity);
    }

    public ConfigEntity insert(ConfigEntity entity) {
        mapper.insert(entity);
        return entity;
    }
}
