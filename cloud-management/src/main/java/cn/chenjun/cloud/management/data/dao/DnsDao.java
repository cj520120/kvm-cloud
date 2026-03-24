package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.DnsEntity;
import cn.chenjun.cloud.management.data.mapper.DnsMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class DnsDao {
    @Autowired
    private DnsMapper mapper;

    public DnsEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<DnsEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public List<DnsEntity> listByNetworkId(int networkId) {
        return mapper.selectList(new QueryWrapper<DnsEntity>().eq(DnsEntity.NETWORK_ID, networkId));
    }

    public Page<DnsEntity> search(int networkId, String keyword, int no, int size) {
        QueryWrapper<DnsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DnsEntity.NETWORK_ID, networkId);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<DnsEntity> wrapper = o;
                wrapper.like(DnsEntity.DNS_IP, condition)
                        .or().like(DnsEntity.DNS_DOMAIN, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<DnsEntity> list = this.mapper.selectList(queryWrapper);
        Page<DnsEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(DnsEntity entity) {
        mapper.updateById(entity);
    }

    public DnsEntity insert(DnsEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public void deleteByNetworkId(int networkId) {
        mapper.delete(new QueryWrapper<DnsEntity>().eq(DnsEntity.NETWORK_ID, networkId));
    }
}
