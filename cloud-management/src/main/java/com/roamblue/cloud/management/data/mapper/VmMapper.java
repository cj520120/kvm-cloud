package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.VmEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface VmMapper extends BaseMapper<VmEntity> {

    @Select(value = "SELECT * FROM tbl_vm_info")
    List<VmEntity> selectAll();

    @Select(value = "SELECT * FROM tbl_vm_info WHERE host_id=#{hostId}")
    List<VmEntity> findByHostId(@Param("hostId") int hostId);

    @Select(value = "SELECT * FROM tbl_vm_info WHERE calculation_scheme_id=#{calculationSchemeId} ")
    List<VmEntity> findByCalculationSchemeId(@Param("calculationSchemeId") int calculationSchemeId);


    @Select(value = "SELECT * FROM tbl_vm_info WHERE vm_name=#{name}")
    VmEntity findByName(@Param("name") String name);

    @Update(value = "UPDATE tbl_vm_info set last_update_time=#{lastUpdateTime} where id=#{vmId}")
    int updateLastActiveTime(@Param("vmId") int id, @Param("lastUpdateTime") Date lastUpdateTime);
}
