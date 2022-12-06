package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface NetworkMapper extends BaseMapper<GuestNetworkEntity> {
    /**
     * 获取网络信息
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_network_info")
    List<GuestNetworkEntity> selectAll();

    /**
     * 获取集群的所有网络
     *
     * @param clusterId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_network_info WHERE cluster_id=#{clusterId}")
    List<GuestNetworkEntity> findByClusterId(@Param("clusterId") int clusterId);
}
