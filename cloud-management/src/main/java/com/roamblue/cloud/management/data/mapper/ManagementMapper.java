package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.ManagementEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagementMapper extends BaseMapper<ManagementEntity> {
    @Select("SELECT * FROM tbl_management_info WHERE server_id=#{serverId}")
    ManagementEntity findByServerId(@Param("serverId") String serverId);
}
