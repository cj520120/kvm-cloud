package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ComponentDao {
    @Autowired
    private ComponentMapper mapper;

    public ComponentEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<ComponentEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(ComponentEntity entity) {
        mapper.updateById(entity);
    }

    public ComponentEntity insert(ComponentEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<ComponentEntity> listByNetworkId(int networkId) {
        return mapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, networkId));
    }

    public List<ComponentEntity> listByIds(List<Integer> componentIds) {
        if (ObjectUtils.isEmpty(componentIds)) {
            return new ArrayList<>();
        }
        return mapper.selectBatchIds(componentIds);
    }
}
