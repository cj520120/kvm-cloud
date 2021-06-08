package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.LockEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface LockMapper extends BaseMapper<LockEntity> {

    @Select(value = "SELECT * FROM tbl_lock_info WHERE lock_name=#{name}")
    LockEntity findByName(@Param("name") String name);
}
