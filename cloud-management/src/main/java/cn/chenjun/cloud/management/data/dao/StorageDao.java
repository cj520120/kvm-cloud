package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class StorageDao {
    @Autowired
    private StorageMapper mapper;

    public StorageEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<StorageEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<StorageEntity> search(Integer storageType, Integer storageStatus, String keyword, int no, int size) {
        QueryWrapper<StorageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(storageType != null, StorageEntity.STORAGE_TYPE, storageType);
        queryWrapper.eq(storageStatus != null, StorageEntity.STORAGE_STATUS, storageStatus);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<StorageEntity> wrapper = o;
                wrapper.like(StorageEntity.STORAGE_NAME, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<StorageEntity> storageList = this.mapper.selectList(queryWrapper);
        Page<StorageEntity> page = Page.create(nCount, nOffset, size);
        page.setList(storageList);
        return page;
    }

    public List<StorageEntity> listByHostId(int hostId) {
        return mapper.selectList(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_HOST_ID, hostId));
    }

    public void deleteByHostId(int hostId) {
        mapper.delete(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_HOST_ID, hostId));
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }


    public void update(StorageEntity entity) {
        mapper.updateById(entity);
    }

    public StorageEntity insert(StorageEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<StorageEntity> listByStatus(int status) {
        return mapper.selectList(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_STATUS, status));
    }

    public StorageEntity findByName(String name) {
        return mapper.selectOne(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_NAME, name));
    }

    public StorageEntity findByLocalStorage(int parentStorageId, int hostId) {
        return mapper.selectOne(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_PARENT_ID, parentStorageId).eq(StorageEntity.STORAGE_HOST_ID, hostId));
    }

    public List<StorageEntity> listLocalStorage() {
        return mapper.selectList(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_HOST_ID, 0).eq(StorageEntity.STORAGE_TYPE, Constant.StorageType.LOCAL));
    }

    public List<StorageEntity> listStorageByParentStorageId(int storageId) {
        return mapper.selectList(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_PARENT_ID, storageId));
    }

}
