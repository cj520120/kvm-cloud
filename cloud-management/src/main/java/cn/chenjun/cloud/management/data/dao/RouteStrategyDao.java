package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.RouteStrategyEntity;
import cn.chenjun.cloud.management.data.mapper.RouteStrategyMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class RouteStrategyDao {
    @Autowired
    private RouteStrategyMapper mapper;


    /**
     * 根据 component_id 获取该组件/网络下的所有路由策略
     */
    public List<RouteStrategyEntity> listByComponentId(int componentId) {
        return mapper.selectList(new QueryWrapper<RouteStrategyEntity>().eq(RouteStrategyEntity.COMPONENT_ID, componentId));
    }


    /**
     * 单个保存
     */
    public RouteStrategyEntity create(int componentId, String destIp, int cidr, String nexthop) {
        RouteStrategyEntity entity = new RouteStrategyEntity();
        entity.setComponentId(componentId);
        entity.setDestIp(destIp);
        entity.setCidr(cidr);
        entity.setNexthop(nexthop);
        mapper.insert(entity);
        return entity;
    }

    /**
     * 根据ID删除
     */
    public void deleteById(long id) {
        mapper.deleteById(id);
    }

    /**
     * 根据 componentId 清空该组件所有路由
     */
    public void deleteByComponentId(int componentId) {
        mapper.delete(new QueryWrapper<RouteStrategyEntity>().eq(RouteStrategyEntity.COMPONENT_ID, componentId));
    }

    public RouteStrategyEntity findById(Integer id) {
        return mapper.selectById(id);
    }

    public Page<RouteStrategyEntity> search(int componentId, String keyword, int no, int size) {
        QueryWrapper<RouteStrategyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(RouteStrategyEntity.COMPONENT_ID, componentId);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<RouteStrategyEntity> wrapper = o;
                wrapper.like(RouteStrategyEntity.DEST_IP, condition)
                        .or().like(RouteStrategyEntity.NEXTHOP, condition);
            });
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<RouteStrategyEntity> list = this.mapper.selectList(queryWrapper);
        Page<RouteStrategyEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }
}
