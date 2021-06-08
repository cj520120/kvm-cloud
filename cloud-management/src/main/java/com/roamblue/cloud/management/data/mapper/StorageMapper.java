package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.StorageEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageMapper extends BaseMapper<StorageEntity> {

    @Select(value = "SELECT * FROM tbl_storage_info")
    List<StorageEntity> selectAll();

    @Select(value = "SELECT * FROM tbl_storage_info WHERE cluster_id=#{clusterId}")
    List<StorageEntity> findByClusterId(@Param("clusterId") int clusterId);
}
