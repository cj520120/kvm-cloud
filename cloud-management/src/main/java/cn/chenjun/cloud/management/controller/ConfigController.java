package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.config.Oauth2Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenjun
 */
@RestController
public class ConfigController {
    @Autowired
    private Oauth2Config config;

    @GetMapping("/api/config")
    public ResultUtil<Map<String, Object>> getSystemConfig() {
        Map<String, Object> oauth = new HashMap<>(3);
        oauth.put("enable", this.config.isEnable());
        oauth.put("title", this.config.getTitle());
        Map<String, Object> map = new HashMap<>(1);
        map.put("oauth", oauth);
        return ResultUtil.<Map<String, Object>>builder().data(map).build();
    }

}
