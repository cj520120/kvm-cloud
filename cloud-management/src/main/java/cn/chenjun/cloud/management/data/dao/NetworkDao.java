package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class NetworkDao {
    @Autowired
    private NetworkMapper mapper;

    public NetworkEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<NetworkEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<NetworkEntity> search(String keyword, int no, int size) {
        QueryWrapper<NetworkEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(NetworkEntity.NETWORK_NAME, condition);
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<NetworkEntity> networkList = this.mapper.selectList(wrapper);
        Page<NetworkEntity> page = Page.create(nCount, nOffset, size);
        page.setList(networkList);
        return page;
    }


    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(NetworkEntity entity) {
        mapper.updateById(entity);
    }

    public NetworkEntity insert(NetworkEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public List<NetworkEntity> listNetworkByIds(List<Integer> networkIds) {
        if (ObjectUtils.isEmpty(networkIds)) {
            return new ArrayList<>();
        }
        return mapper.selectBatchIds(networkIds);
    }

    public List<NetworkEntity> listByStatus(int status) {
        return mapper.selectList(new QueryWrapper<NetworkEntity>().eq(NetworkEntity.NETWORK_STATUS, status));
    }
}
