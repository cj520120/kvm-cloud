package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.VncEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface VncMapper extends BaseMapper<VncEntity> {


    /**
     * 获取VNC
     *
     * @param clusterId
     * @param networkId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vnc_info WHERE cluster_id=#{clusterId} and network_id=#{networkId}")
    List<VncEntity> findByClusterIdAndNetwork(@Param("clusterId") int clusterId, @Param("networkId") int networkId);

    /**
     * 获取VNC
     *
     * @param vmId
     * @param networkId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vnc_info WHERE vm_id=#{vmId} and network_id=#{networkId}")
    VncEntity findByVmIdAndNetwork(@Param("vmId") int vmId, @Param("networkId") int networkId);

    /**
     * 获取VM的VNC
     *
     * @param vmId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vnc_info WHERE vm_id=#{vmId}")
    List<VncEntity> findByVmId(@Param("vmId") int vmId);

    /**
     * 删除VNC信息
     *
     * @param vmId
     */
    @Select(value = "DELETE FROM tbl_vnc_info WHERE vm_id=#{vmId}")
    void deleteByVmId(@Param("vmId") int vmId);
}