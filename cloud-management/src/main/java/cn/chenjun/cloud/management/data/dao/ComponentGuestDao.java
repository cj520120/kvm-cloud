package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentGuestMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ComponentGuestDao {
    @Autowired
    private ComponentGuestMapper mapper;

    public ComponentGuestEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<ComponentGuestEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(ComponentGuestEntity entity) {
        mapper.updateById(entity);
    }

    public ComponentGuestEntity insert(ComponentGuestEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public void deleteByComponentId(int componentId) {
        mapper.delete(new QueryWrapper<ComponentGuestEntity>().eq(ComponentGuestEntity.COMPONENT_ID, componentId));
    }

    public List<ComponentGuestEntity> listByComponentId(Integer componentId) {
        return mapper.selectList(new QueryWrapper<ComponentGuestEntity>().eq(ComponentGuestEntity.COMPONENT_ID, componentId));
    }

    public ComponentGuestEntity findByGuestId(Integer guestId) {
        return mapper.selectOne(new QueryWrapper<ComponentGuestEntity>().eq(ComponentGuestEntity.GUEST_ID, guestId));
    }

    public List<ComponentGuestEntity> listByComponentIdAndHostId(int componentId, int hostId) {
        return mapper.selectList(new QueryWrapper<ComponentGuestEntity>()
                .eq(ComponentGuestEntity.COMPONENT_ID, componentId)
                .eq(ComponentGuestEntity.HOST_ID, hostId));
    }

    public void deleteByGuestId(Integer guestId) {
        mapper.delete(new QueryWrapper<ComponentGuestEntity>().eq(ComponentGuestEntity.GUEST_ID, guestId));
    }
}
