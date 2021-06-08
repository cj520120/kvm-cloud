package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.CalculationSchemeEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationSchemeMapper extends BaseMapper<CalculationSchemeEntity> {

    @Select(value = "SELECT * FROM tbl_calculation_scheme")
    List<CalculationSchemeEntity> selectAll();
}
