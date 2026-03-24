package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.GroupEntity;
import cn.chenjun.cloud.management.data.mapper.GroupMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GroupDao {
    @Autowired
    private GroupMapper mapper;

    public GroupEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<GroupEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<GroupEntity> search(String keyword, int no, int size) {
        QueryWrapper<GroupEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(GroupEntity.GROUP_NAME, condition);
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<GroupEntity> list = this.mapper.selectList(wrapper);
        Page<GroupEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(GroupEntity entity) {
        mapper.updateById(entity);
    }

    public GroupEntity insert(GroupEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<GroupEntity> listByIds(List<Integer> groupIds) {
        if (ObjectUtils.isEmpty(groupIds)) {
            return new ArrayList<>();
        }
        return mapper.selectBatchIds(groupIds);
    }
}
