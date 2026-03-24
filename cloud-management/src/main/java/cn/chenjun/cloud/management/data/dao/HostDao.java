package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class HostDao {
    @Autowired
    private HostMapper mapper;

    public HostEntity findById(int id) {
        return mapper.selectById(id);
    }

    public HostEntity findByClientId(String clientId) {
        QueryWrapper<HostEntity> wrapper = new QueryWrapper<HostEntity>().eq(HostEntity.CLIENT_ID, clientId);
        return mapper.selectOne(wrapper);
    }

    public List<HostEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<HostEntity> search(String keyword, int no, int size) {
        QueryWrapper<HostEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(HostEntity.HOST_DISPLAY_NAME, condition)
                    .or().like(HostEntity.HOST_IP, condition)
                    .or().like(HostEntity.HOST_OS_NAME, condition);

        }
        int nCount = Math.toIntExact(this.mapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<HostEntity> list = this.mapper.selectList(wrapper);
        Page<HostEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(HostEntity entity) {
        mapper.updateById(entity);
    }

    public HostEntity insert(HostEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<HostEntity> listByIds(List<Integer> hostIds) {
        if (ObjectUtils.isEmpty(hostIds)) {
            return new ArrayList<>();
        }
        return mapper.selectBatchIds(hostIds);
    }

    public List<HostEntity> listByStatus(int status) {
        return mapper.selectList(new QueryWrapper<HostEntity>().eq(HostEntity.HOST_STATUS, status));
    }
}
