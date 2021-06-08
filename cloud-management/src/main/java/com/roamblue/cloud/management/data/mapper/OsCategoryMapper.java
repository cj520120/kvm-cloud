package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.OsCategoryEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsCategoryMapper extends BaseMapper<OsCategoryEntity> {

    @Select(value = "SELECT * FROM tbl_os_category")
    List<OsCategoryEntity> selectAll();
}
