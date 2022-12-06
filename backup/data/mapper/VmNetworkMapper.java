package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.VmNetworkEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface VmNetworkMapper extends BaseMapper<VmNetworkEntity> {

    /**
     * 获取VM网络配置
     *
     * @param vmId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_network WHERE vm_id=#{vmId} ORDER BY vm_device ASC")
    List<VmNetworkEntity> findByVmId(@Param("vmId") int vmId);

    /**
     * 根据分类获取可以分配的IP
     *
     * @param networkId
     * @param ipType
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_network WHERE network_id=#{networkId} and ip_type=#{ipType} and vm_id=0")
    List<VmNetworkEntity> findEmptyNetworkByNetworkIdAndIpType(@Param("networkId") int networkId, @Param("ipType") String ipType);

    /**
     * 根据网络ID获取所有网络信息
     *
     * @param networkId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_network WHERE network_id=#{networkId}")
    List<VmNetworkEntity> findByNetworkId(@Param("networkId") int networkId);

    /**
     * 释放VM的网络信息
     *
     * @param vmId
     * @return
     */
    @Update(value = "UPDATE tbl_vm_network SET vm_id=0 WHERE vm_id=#{vmId}")
    int freeByVmId(@Param("vmId") int vmId);

    /**
     * 释放VM的网络信息
     *
     * @param vmId
     * @param id
     * @return
     */
    @Update(value = "UPDATE tbl_vm_network SET vm_id=0 WHERE vm_id=#{vmId} and id=#{id}")
    int freeByVmIdAndId(@Param("vmId") int vmId, @Param("id") int id);

    /**
     * 申请网络信息
     *
     * @param id
     * @param vmId
     * @param deviceId
     * @return
     */
    @Update(value = "UPDATE tbl_vm_network SET vm_id=#{vmId},vm_device=#{deviceId} WHERE id=#{id} and vm_id=0")
    int allocateNetwork(@Param("id") int id, @Param("vmId") int vmId, @Param("deviceId") int deviceId);


}
