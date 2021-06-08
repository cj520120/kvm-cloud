package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.VmNetworkEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VmNetworkMapper extends BaseMapper<VmNetworkEntity> {


    @Select(value = "SELECT * FROM tbl_vm_network WHERE vm_id=#{vmId} ORDER BY vm_device ASC")
    List<VmNetworkEntity> findByVmId(@Param("vmId") int vmId);

    @Select(value = "SELECT * FROM tbl_vm_network WHERE network_id=#{networkId} and ip_type=#{ipType}")
    List<VmNetworkEntity> findByNetworkIdAndIpType(@Param("networkId") int networkId, @Param("ipType") String ipType);

    @Select(value = "SELECT * FROM tbl_vm_network WHERE network_id=#{networkId}")
    List<VmNetworkEntity> findByNetworkId(@Param("networkId") int networkId);

    @Update(value = "UPDATE tbl_vm_network SET vm_id=0 WHERE vm_id=#{vmId}")
    int freeByVmId(@Param("vmId") int vmId);

    @Update(value = "UPDATE tbl_vm_network SET vm_id=#{vmId} WHERE id=#{id} and vm_id=0")
    int allocateNetwork(@Param("id") int id, @Param("vmId") int vmId);


}
