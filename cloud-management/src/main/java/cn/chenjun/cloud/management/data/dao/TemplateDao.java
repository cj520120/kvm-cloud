package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.mapper.TemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TemplateDao {
    @Autowired
    private TemplateMapper mapper;

    public TemplateEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<TemplateEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<TemplateEntity> search(Integer templateType, Integer templateStatus, String keyword, int no, int size) {
        QueryWrapper<TemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(templateType != null, TemplateEntity.TEMPLATE_TYPE, templateType);
        queryWrapper.eq(templateStatus != null, TemplateEntity.TEMPLATE_STATUS, templateStatus);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<TemplateEntity> wrapper = o;
                wrapper.like(TemplateEntity.TEMPLATE_NAME, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<TemplateEntity> list = this.mapper.selectList(queryWrapper);
        Page<TemplateEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(TemplateEntity entity) {
        mapper.updateById(entity);
    }

    public TemplateEntity insert(TemplateEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<TemplateEntity> listByIds(List<Integer> templateIds) {
        if (ObjectUtils.isEmpty(templateIds)) {
            return new ArrayList<>();
        }
        return this.mapper.selectBatchIds(templateIds);
    }
}
