package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.CalculationSchemeInfo;

import java.util.List;

public interface CalculationSchemeService {
    /**
     * 计算方案列表
     *
     * @return
     */
    List<CalculationSchemeInfo> listCalculationScheme();

    /**
     * 查找计算方案
     *
     * @param id
     * @return
     */
    CalculationSchemeInfo findCalculationSchemeById(int id);

    /**
     * 获取默认计算方案
     *
     * @return
     */
    CalculationSchemeInfo getDefaultCalculationScheme();

    /**
     * 创建默认计算方案
     *
     * @param name
     * @param cpu
     * @param speed
     * @param memory
     * @return
     */
    CalculationSchemeInfo createCalculationScheme(String name, int cpu, int speed, long memory);

    /**
     * 销毁计算方案
     *
     * @param id
     */
    void destroyCalculationSchemeById(int id);

}
