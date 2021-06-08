package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.NetworkEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkMapper extends BaseMapper<NetworkEntity> {
    @Select(value = "SELECT * FROM tbl_network_info")
    List<NetworkEntity> selectAll();

    @Select(value = "SELECT * FROM tbl_network_info WHERE cluster_id=#{clusterId}")
    List<NetworkEntity> findByClusterId(@Param("clusterId") int clusterId);
}
