package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.GuestSchemeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface CalculationSchemeMapper extends BaseMapper<GuestSchemeEntity> {

    /**
     * 获取所有计算方案
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_calculation_scheme")
    List<GuestSchemeEntity> selectAll();
}
