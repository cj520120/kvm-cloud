package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.VncEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VncMapper extends BaseMapper<VncEntity> {


    @Select(value = "SELECT * FROM tbl_vnc_info WHERE cluster_id=#{clusterId} and network_id=#{networkId}")
    List<VncEntity> findByClusterIdAndNetwork(@Param("clusterId") int clusterId, @Param("networkId") int networkId);

    @Select(value = "SELECT * FROM tbl_vnc_info WHERE vm_id=#{vmId} and network_id=#{networkId}")
    VncEntity findByVmIdAndNetwork(@Param("vmId") int vmId, @Param("networkId") int networkId);

    @Select(value = "SELECT * FROM tbl_vnc_info WHERE vm_id=#{vmId}")
    List<VncEntity> findByVmId(@Param("vmId") int vmId);

    @Select(value = "DELETE FROM tbl_vnc_info WHERE vm_id=#{vmId}")
    void deleteByVmId(@Param("vmId") int vmId);
}
