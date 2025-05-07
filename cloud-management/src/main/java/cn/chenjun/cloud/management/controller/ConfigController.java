package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.ConfigModel;
import cn.chenjun.cloud.management.model.SystemConfigModel;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@RestController
public class ConfigController extends BaseController {
    @Autowired
    private ConfigService configService;

    @GetMapping("/api/config")
    public ResultUtil<SystemConfigModel> getSystemConfig() {
        SystemConfigModel.Oauth2 oauth2 = SystemConfigModel.Oauth2.builder()
                .enable(Constant.Enable.YES.equals(this.configService.getConfig(ConfigKey.OAUTH2_ENABLE)))
                .title(this.configService.getConfig(ConfigKey.OAUTH2_TITLE))
                .build();
        SystemConfigModel model = SystemConfigModel.builder().oauth2(oauth2).baseUri(this.configService.getConfig(ConfigKey.DEFAULT_BASE_URI)).build();
        return ResultUtil.success(model);
    }

    @LoginRequire
    @GetMapping("/api/config/search")
    public ResultUtil<List<ConfigModel>> listConfig(@RequestParam("allocateType") int allocateType, @RequestParam("allocateId") int allocateId) {
        return this.lockRun(() -> this.configService.listConfig(allocateType, allocateId));
    }

    @LoginRequire
    @PutMapping("/api/config/create")
    public ResultUtil<ConfigModel> createConfig(@RequestParam("configKey") String configKey,
                                                @RequestParam("allocateType") int allocateType,
                                                @RequestParam("allocateId") int allocateId,
                                                @RequestParam("configValue") String configValue) {
        return this.lockRun(() -> this.configService.createConfig(configKey, allocateType, allocateId, configValue));
    }

    @LoginRequire
    @PostMapping("/api/config/update")
    public ResultUtil<ConfigModel> updateConfig(@RequestParam("configKey") String configKey,
                                                @RequestParam("allocateType") int allocateType,
                                                @RequestParam("allocateId") int allocateId,
                                                @RequestParam("configValue") String configValue) {
        return this.lockRun(() -> this.configService.updateConfig(configKey, allocateType, allocateId, configValue));
    }

    @LoginRequire
    @DeleteMapping("/api/config/destroy")
    public ResultUtil<ConfigModel> deleteConfig(@RequestParam("id") int id) {
        return this.lockRun(() -> this.configService.deleteConfig(id));
    }
}
