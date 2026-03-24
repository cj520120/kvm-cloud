package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.data.mapper.TemplateVolumeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class TemplateVolumeDao {
    @Autowired
    private TemplateVolumeMapper mapper;

    public TemplateVolumeEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<TemplateVolumeEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(TemplateVolumeEntity entity) {
        mapper.updateById(entity);
    }

    public TemplateVolumeEntity insert(TemplateVolumeEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<TemplateVolumeEntity> listByStorageId(int storageId) {
        return mapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.STORAGE_ID, storageId));
    }

    public void deleteByTemplateId(Integer templateId) {
        mapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, templateId));
    }

    public List<TemplateVolumeEntity> listByTemplateId(Integer templateId) {
        return mapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, templateId));
    }

    public void deleteByStorageId(int storageId) {
        mapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.STORAGE_ID, storageId));
    }

    public void deleteBatchIds(Collection<Integer> collect) {
        mapper.deleteBatchIds(collect);
    }
}
