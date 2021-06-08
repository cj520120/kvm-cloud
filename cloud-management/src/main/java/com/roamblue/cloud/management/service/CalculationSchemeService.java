package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.CalculationSchemeInfo;

import java.util.List;

public interface CalculationSchemeService {
    List<CalculationSchemeInfo> listCalculationScheme();

    CalculationSchemeInfo findCalculationSchemeById(int id);

    CalculationSchemeInfo getDefaultCalculationScheme();

    CalculationSchemeInfo createCalculationScheme(String name, int cpu, int speed, long memory);

    void destroyCalculationSchemeById(int id);

}
