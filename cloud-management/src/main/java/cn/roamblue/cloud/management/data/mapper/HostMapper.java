package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.HostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface HostMapper extends BaseMapper<HostEntity> {
    /**
     * 获取所有主机
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_host_info")
    List<HostEntity> selectAll();

    /**
     * 获取集群的所有主机
     *
     * @param clusterId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_host_info WHERE cluster_id=#{clusterId}")
    List<HostEntity> findByClusterId(@Param("clusterId") int clusterId);

}
