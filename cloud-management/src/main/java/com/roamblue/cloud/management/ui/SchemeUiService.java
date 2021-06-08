package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.CalculationSchemeInfo;

import java.util.List;

public interface SchemeUiService {
    ResultUtil<List<CalculationSchemeInfo>> listScheme();

    ResultUtil<CalculationSchemeInfo> findSchemeById(int id);

    ResultUtil<CalculationSchemeInfo> createScheme(String name, int cpu, int speed, long memory);

    ResultUtil<Void> destroyScheme(int id);
}
