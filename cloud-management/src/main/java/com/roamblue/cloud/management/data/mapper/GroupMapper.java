package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.GroupEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface GroupMapper extends BaseMapper<GroupEntity> {
    /**
     * 获取所有群组信息
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_group_info")
    List<GroupEntity> selectAll();
}
