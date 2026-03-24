package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GuestNetworkDao {
    @Autowired
    private GuestNetworkMapper mapper;

    public GuestNetworkEntity findById(int id) {
        return mapper.selectById(id);
    }

    public GuestNetworkEntity findByIp(int networkId, String ip) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId);
        return mapper.selectOne(wrapper);
    }

    public int countByAllocateGuest(int networkId) {
        return Math.toIntExact(mapper.selectCount(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST).ne(GuestNetworkEntity.ALLOCATE_ID, 0)));
    }

    public List<GuestNetworkEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public List<GuestNetworkEntity> listByGuestId(int guestId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(GuestNetworkEntity.ALLOCATE_ID, guestId);
        wrapper.eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST);
        return mapper.selectList(wrapper);
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(GuestNetworkEntity entity) {
        mapper.updateById(entity);
    }

    public GuestNetworkEntity insert(GuestNetworkEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public void deleteByNetworkId(int networkId) {
        mapper.delete(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId));
    }

    public List<GuestNetworkEntity> listByAllocate(int allocateType, int allocateId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, allocateId).eq(GuestNetworkEntity.ALLOCATE_TYPE, allocateType);
        return mapper.selectList(wrapper);
    }

    public List<GuestNetworkEntity> listByNetworkId(int networkId) {
        return mapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_ID, networkId));
    }

    public GuestNetworkEntity allocate(int networkId) {
        QueryWrapper<GuestNetworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(GuestNetworkEntity.NETWORK_ID, networkId)
                .eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.DEFAULT)
                .last("LIMIT 1");
        return mapper.selectOne(wrapper);
    }
}
