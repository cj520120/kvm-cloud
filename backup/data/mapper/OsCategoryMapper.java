package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.OsCategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
