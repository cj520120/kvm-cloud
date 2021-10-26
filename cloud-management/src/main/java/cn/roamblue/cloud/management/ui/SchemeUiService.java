package cn.roamblue.cloud.management.ui;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.bean.CalculationSchemeInfo;

import java.util.List;

/**
 * @author chenjun
 */
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
     * @param socket
     * @param core
     * @param threads
     */
    ResultUtil<CalculationSchemeInfo> createScheme(String name, int cpu, int speed, long memory,int socket,int core,int threads);

    /**
     * 销毁计算方案
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyScheme(int id);
}
