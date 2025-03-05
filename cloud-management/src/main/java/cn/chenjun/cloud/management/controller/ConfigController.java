package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.ConfigModel;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@RestController
public class ConfigController extends BaseController {
    @Autowired
    private ConfigService configService;

    @GetMapping("/api/config")
    public ResultUtil<Map<String, Object>> getSystemConfig() {
        Map<String, Object> oauth = new HashMap<>(3);
        oauth.put("enable", Constant.Enable.YES.equals(this.configService.getConfig(ConfigKey.OAUTH2_ENABLE)));
        oauth.put("title", this.configService.getConfig(ConfigKey.OAUTH2_TITLE));
        Map<String, Object> map = new HashMap<>(1);
        map.put("oauth", oauth);
        return ResultUtil.<Map<String, Object>>builder().data(map).build();
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
