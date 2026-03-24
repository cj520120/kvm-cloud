package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.NatEntity;
import cn.chenjun.cloud.management.data.mapper.NatMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class NatDao {
    @Autowired
    private NatMapper mapper;

    public NatEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<NatEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<NatEntity> search(String keyword, int componentId, int no, int size) {
        QueryWrapper<NatEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(NatEntity.COMPONENT_ID, componentId);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<NatEntity> wrapper = o;
                wrapper.like(NatEntity.LOCAL_PORT, condition)
                        .or().like(NatEntity.PROTOCOL, condition)
                        .or().like(NatEntity.REMOTE_IP, condition)
                        .or().like(NatEntity.REMOTE_PORT, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<NatEntity> list = this.mapper.selectList(queryWrapper);
        Page<NatEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;

    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(NatEntity entity) {
        mapper.updateById(entity);
    }

    public NatEntity insert(NatEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public void deleteByComponentId(Integer componentId) {
        mapper.delete(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, componentId));
    }

    public List<NatEntity> listByComponentId(int componentId) {
        return mapper.selectList(new QueryWrapper<NatEntity>().eq(NatEntity.COMPONENT_ID, componentId));
    }
}
