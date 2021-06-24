package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.CalculationSchemeEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface CalculationSchemeMapper extends BaseMapper<CalculationSchemeEntity> {

    /**
     * 获取所有计算方案
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_calculation_scheme")
    List<CalculationSchemeEntity> selectAll();
}
