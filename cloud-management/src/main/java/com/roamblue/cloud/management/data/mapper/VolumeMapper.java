package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.VolumeEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeMapper extends BaseMapper<VolumeEntity> {
    @Select(value = "SELECT * FROM tbl_volume_info")
    List<VolumeEntity> selectAll();

    @Select(value = "SELECT * FROM tbl_volume_info WHERE cluster_id=#{clusterId}")
    List<VolumeEntity> findByClusterId(@Param("clusterId") int clusterId);

    @Select(value = "SELECT * FROM tbl_volume_info WHERE vm_id=#{vmId} ORDER BY vm_device ASC")
    List<VolumeEntity> findByVmId(@Param("vmId") int vmId);

    @Select(value = "SELECT * FROM tbl_volume_info WHERE storage_id=#{storageId} ")
    List<VolumeEntity> findByStorageId(@Param("storageId") int storageId);

}
