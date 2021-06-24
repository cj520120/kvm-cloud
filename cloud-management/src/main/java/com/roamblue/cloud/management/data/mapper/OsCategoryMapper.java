package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.OsCategoryEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface OsCategoryMapper extends BaseMapper<OsCategoryEntity> {
    /**
     * 获取系统分类
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_os_category")
    List<OsCategoryEntity> selectAll();
}
