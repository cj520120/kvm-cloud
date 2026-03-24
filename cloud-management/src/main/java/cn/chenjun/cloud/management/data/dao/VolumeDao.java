package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.data.mapper.VolumeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class VolumeDao {
    @Autowired
    private VolumeMapper mapper;

    public VolumeEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<VolumeEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<VolumeEntity> search(Integer storageId, Integer status, Integer templateId, String volumeType, String keyword, int no, int size) {
        QueryWrapper<VolumeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(storageId != null, VolumeEntity.STORAGE_ID, storageId);
        queryWrapper.eq(templateId != null, VolumeEntity.TEMPLATE_ID, templateId);
        queryWrapper.eq(status != null, VolumeEntity.VOLUME_STATUS, status);
        queryWrapper.eq(!ObjectUtils.isEmpty(volumeType), VolumeEntity.VOLUME_TYPE, volumeType);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<VolumeEntity> wrapper = o;
                wrapper.like(VolumeEntity.VOLUME_NAME, condition)
                        .or().like(VolumeEntity.VOLUME_DESCRIPTION, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<VolumeEntity> volumeList = this.mapper.selectList(queryWrapper);
        Page<VolumeEntity> page = Page.create(nCount, nOffset, size);
        page.setList(volumeList);
        return page;

    }

    public List<VolumeEntity> listByGuestId(int guestId) {
        QueryWrapper<VolumeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(VolumeEntity.GUEST_ID, guestId);
        return mapper.selectList(wrapper);
    }

    public VolumeEntity findByGuestIdAndDevice(int guestId, int device) {
        QueryWrapper<VolumeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(VolumeEntity.GUEST_ID, guestId).eq(VolumeEntity.DEVICE_ID, device);
        return mapper.selectOne(wrapper);
    }

    public int countByStorageId(int storageId) {
        QueryWrapper<VolumeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(VolumeEntity.STORAGE_ID, storageId);
        return Math.toIntExact(mapper.selectCount(wrapper));
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(VolumeEntity entity) {
        mapper.updateById(entity);
    }

    public VolumeEntity insert(VolumeEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<VolumeEntity> listByStorageId(int storageId) {
        return mapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.STORAGE_ID, storageId));
    }

    public List<VolumeEntity> listByStorageIdAndStatus(int storageId, int status) {
        return mapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.STORAGE_ID, storageId).eq(VolumeEntity.VOLUME_STATUS, status));
    }

    public VolumeEntity findByName(String name) {
        return mapper.selectOne(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.VOLUME_NAME, name));
    }
}
