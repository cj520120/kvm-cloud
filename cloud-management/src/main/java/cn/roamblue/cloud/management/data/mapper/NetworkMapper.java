package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface NetworkMapper extends BaseMapper<NetworkEntity> {
    /**
     * 获取网络信息
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_network_info")
    List<NetworkEntity> selectAll();

    /**
     * 获取集群的所有网络
     *
     * @param clusterId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_network_info WHERE cluster_id=#{clusterId}")
    List<NetworkEntity> findByClusterId(@Param("clusterId") int clusterId);
}
