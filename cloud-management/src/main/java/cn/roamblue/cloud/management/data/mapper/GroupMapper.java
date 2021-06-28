package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.GroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
