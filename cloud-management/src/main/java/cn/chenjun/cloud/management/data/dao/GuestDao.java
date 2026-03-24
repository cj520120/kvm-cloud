package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GuestDao {
    @Autowired
    private GuestMapper mapper;

    public GuestEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<GuestEntity> findByIds(List<Integer> ids) {
        return mapper.selectBatchIds(ids);
    }

    public List<GuestEntity> listByNetworkId(int networkId) {
        QueryWrapper<GuestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(GuestEntity.NETWORK_ID, networkId);
        return mapper.selectList(wrapper);
    }

    public List<GuestEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<GuestEntity> search(Integer guestType, Integer groupId, Integer networkId, Integer hostId, Integer schemeId, Integer status, String keyword, int no, int size) {
        QueryWrapper queryWrapper = new QueryWrapper<GuestEntity>();
        queryWrapper.eq(groupId != null, GuestEntity.GROUP_ID, groupId);
        queryWrapper.eq(hostId != null, GuestEntity.HOST_ID, hostId);
        queryWrapper.eq(networkId != null, GuestEntity.NETWORK_ID, networkId);
        queryWrapper.eq(schemeId != null, GuestEntity.SCHEME_ID, schemeId);
        queryWrapper.eq(guestType != null, GuestEntity.GUEST_TYPE, guestType);
        queryWrapper.eq(status != null, GuestEntity.GUEST_STATUS, status);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<GuestEntity> wrapper = (QueryWrapper) o;
                wrapper.like(GuestEntity.GUEST_IP, condition)
                        .or().like(GuestEntity.GUEST_NAME, condition)
                        .or().like(GuestEntity.GUEST_DESCRIPTION, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<GuestEntity> guestList = this.mapper.selectList(queryWrapper);
        Page<GuestEntity> page = Page.create(nCount, nOffset, size);
        page.setList(guestList);
        return page;
    }

    public List<GuestEntity> listComponentGuest(int componentId) {
        QueryWrapper<GuestEntity> wrapper = new QueryWrapper<GuestEntity>().eq(GuestEntity.OTHER_ID, componentId).eq(GuestEntity.GUEST_TYPE, cn.chenjun.cloud.common.util.Constant.GuestType.COMPONENT);
        return mapper.selectList(wrapper);
    }

    public int countByHostId(int hostId) {
        QueryWrapper<GuestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(GuestEntity.BIND_HOST_ID, hostId);
        wrapper.or().eq(GuestEntity.HOST_ID, hostId);
        return Math.toIntExact(mapper.selectCount(wrapper));
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(GuestEntity entity) {
        mapper.updateById(entity);
    }

    public GuestEntity insert(GuestEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<GuestEntity> listRunningByHostId(int hostId) {
        QueryWrapper<GuestEntity> wrapper = new QueryWrapper<>();
        wrapper.or().eq(GuestEntity.HOST_ID, hostId);
        return mapper.selectList(wrapper);
    }

    public List<GuestEntity> selectByIds(List<Integer> guestIds) {
        if (ObjectUtils.isEmpty(guestIds)) {
            return new ArrayList<>();
        }
        return mapper.selectBatchIds(guestIds);
    }

    public void detachCdByTemplateId(Integer templateId) {
        mapper.detachCdByTemplateId(templateId);
    }

    public List<GuestEntity> listByNames(List<String> guestNames) {
        QueryWrapper<GuestEntity> wrapper = new QueryWrapper<>();
        wrapper.in(GuestEntity.GUEST_NAME, guestNames);
        return mapper.selectList(wrapper);
    }

    public List<GuestEntity> listByHostId(int hostId) {
        QueryWrapper<GuestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(GuestEntity.HOST_ID, hostId);
        return mapper.selectList(wrapper);
    }
}
