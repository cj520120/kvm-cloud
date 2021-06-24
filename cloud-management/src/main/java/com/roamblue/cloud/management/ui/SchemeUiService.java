package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.CalculationSchemeInfo;

import java.util.List;

public interface SchemeUiService {
    /**
     * 计算方案列表
     *
     * @return
     */
    ResultUtil<List<CalculationSchemeInfo>> listScheme();

    /**
     * 根据ID获取计算方案
     *
     * @param id
     * @return
     */
    ResultUtil<CalculationSchemeInfo> findSchemeById(int id);

    /**
     * 创建计算方案
     *
     * @param name
     * @param cpu
     * @param speed
     * @param memory
     * @return
     */
    ResultUtil<CalculationSchemeInfo> createScheme(String name, int cpu, int speed, long memory);

    /**
     * 销毁计算方案
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyScheme(int id);
}
