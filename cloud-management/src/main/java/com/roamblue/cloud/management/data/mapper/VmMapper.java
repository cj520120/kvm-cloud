package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.VmEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface VmMapper extends BaseMapper<VmEntity> {

    /**
     * 获取VM列表
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_info")
    List<VmEntity> selectAll();

    /**
     * 根据主机ID获取VM
     *
     * @param hostId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_info WHERE host_id=#{hostId}")
    List<VmEntity> findByHostId(@Param("hostId") int hostId);

    /**
     * 根据计算方案获取VM
     *
     * @param calculationSchemeId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_info WHERE calculation_scheme_id=#{calculationSchemeId} ")
    List<VmEntity> findByCalculationSchemeId(@Param("calculationSchemeId") int calculationSchemeId);

    /**
     * 根据名称查询VM
     *
     * @param name
     * @return
     */
    @Select(value = "SELECT * FROM tbl_vm_info WHERE vm_name=#{name}")
    VmEntity findByName(@Param("name") String name);

    /**
     * 更新VM最后变更时间
     *
     * @param id
     * @param lastUpdateTime
     * @return
     */
    @Update(value = "UPDATE tbl_vm_info set last_update_time=#{lastUpdateTime} where id=#{vmId}")
    int updateLastActiveTime(@Param("vmId") int id, @Param("lastUpdateTime") Date lastUpdateTime);
}
