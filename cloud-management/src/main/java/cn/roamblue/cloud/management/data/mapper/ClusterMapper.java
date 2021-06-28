package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface ClusterMapper extends BaseMapper<ClusterEntity> {
    /**
     * 获取所有集群信息
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_cluster_info")
    List<ClusterEntity> selectAll();
}
