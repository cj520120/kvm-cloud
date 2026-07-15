package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.management.data.entity.HostPciDeviceEntity;
import cn.chenjun.cloud.management.data.mapper.HostPciDeviceMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HostPciDeviceDao {
    @Autowired
    private HostPciDeviceMapper mapper;

    public HostPciDeviceEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<HostPciDeviceEntity> listPciDeviceByGuestId(int guestId) {
        return mapper.selectList(new QueryWrapper<HostPciDeviceEntity>().eq(HostPciDeviceEntity.GUEST_ID, guestId));
    }

    public HostPciDeviceEntity insert(HostPciDeviceEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public int deleteByGuestId(int guestId) {
        return mapper.delete(new QueryWrapper<HostPciDeviceEntity>().eq(HostPciDeviceEntity.GUEST_ID, guestId));
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

}
