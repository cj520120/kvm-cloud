package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.ClusterEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterMapper extends BaseMapper<ClusterEntity> {

    @Select(value = "SELECT * FROM tbl_cluster_info")
    List<ClusterEntity> selectAll();
}
