package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.SshAuthorizedEntity;
import cn.chenjun.cloud.management.data.mapper.SshAuthorizedMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class SshAuthorizedDao {
    @Autowired
    private SshAuthorizedMapper mapper;

    public SshAuthorizedEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<SshAuthorizedEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<SshAuthorizedEntity> search(String keyword, int no, int size) {
        QueryWrapper<SshAuthorizedEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(SshAuthorizedEntity.SSH_NAME, condition);
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<SshAuthorizedEntity> list = this.mapper.selectList(wrapper);
        Page<SshAuthorizedEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(SshAuthorizedEntity entity) {
        mapper.updateById(entity);
    }

    public SshAuthorizedEntity insert(SshAuthorizedEntity entity) {
        mapper.insert(entity);
        return entity;
    }
}
