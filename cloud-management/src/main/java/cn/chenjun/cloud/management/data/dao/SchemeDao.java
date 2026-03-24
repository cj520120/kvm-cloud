package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.SchemeEntity;
import cn.chenjun.cloud.management.data.mapper.SchemeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SchemeDao {
    @Autowired
    private SchemeMapper mapper;

    public SchemeEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<SchemeEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<SchemeEntity> search(String keyword, int no, int size) {
        QueryWrapper<SchemeEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(SchemeEntity.SCHEME_NAME, condition);
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<SchemeEntity> list = this.mapper.selectList(wrapper);
        Page<SchemeEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(SchemeEntity entity) {
        mapper.updateById(entity);
    }

    public SchemeEntity insert(SchemeEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<SchemeEntity> listByIds(List<Integer> schemeIds) {
        if (ObjectUtils.isEmpty(schemeIds)) {
            return new ArrayList<>();
        }
        return mapper.selectBatchIds(schemeIds);
    }
}
