package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.CalculationSchemeInfo;
import cn.roamblue.cloud.management.service.CalculationSchemeService;
import cn.roamblue.cloud.management.ui.SchemeUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class SchemeUiServiceImpl extends AbstractUiService implements SchemeUiService {
    @Autowired
    private CalculationSchemeService calculationSchemeService;

    @Override
    public ResultUtil<List<CalculationSchemeInfo>> listScheme() {
        return super.call(calculationSchemeService::listCalculationScheme);
    }

    @Override
    public ResultUtil<CalculationSchemeInfo> findSchemeById(int id) {
        return super.call(() -> calculationSchemeService.findCalculationSchemeById(id));
    }

    @Rule(permissions = "scheme.create")
    @Override
    public ResultUtil<CalculationSchemeInfo> createScheme(String name, int cpu, int speed, long memory) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("CALCULATION_SCHEME_NAME_EMPTY", "名称不能为空"));
        }
        if (cpu <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("CALCULATION_SCHEME_CPU_MUST_GT_0", "CPU必须大于0"));
        }
        if (speed < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("CALCULATION_SCHEME_CPU_SPEED_MUST_GT_0", "CPU主频必须大于等于0(当为0时表示系统默认)"));
        }
        return super.call(() -> calculationSchemeService.createCalculationScheme(name, cpu, speed, memory));
    }

    @Rule(permissions = "scheme.destroy")
    @Override
    public ResultUtil<Void> destroyScheme(int id) {
        return super.call(() -> calculationSchemeService.destroyCalculationSchemeById(id));
    }
}
