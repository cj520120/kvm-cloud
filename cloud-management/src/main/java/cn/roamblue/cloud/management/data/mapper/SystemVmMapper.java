package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.SystemVmEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author chenjun
 */
@Repository
public interface SystemVmMapper extends BaseMapper<SystemVmEntity> {
    /**
     * 获取系统VM信息
     *
     * @param networkId
     * @param vmType
     * @return
     */
    @Select("SELECT * FROM tbl_sys_vm_info WHERE network_id=#{networkId} AND vm_type=#{vmType}")
    SystemVmEntity findByNetworkIdAndVmType(@Param("networkId") int networkId, @Param("vmType") String vmType);
}
