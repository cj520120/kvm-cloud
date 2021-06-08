package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.HostEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostMapper extends BaseMapper<HostEntity> {
    @Select(value = "SELECT * FROM tbl_host_info")
    List<HostEntity> selectAll();

    @Select(value = "SELECT * FROM tbl_host_info WHERE cluster_id=#{clusterId}")
    List<HostEntity> findByClusterId(@Param("clusterId") int clusterId);

}
