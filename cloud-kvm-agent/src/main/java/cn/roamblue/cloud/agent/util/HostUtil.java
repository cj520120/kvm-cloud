package cn.roamblue.cloud.agent.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.AppUtils;
import cn.roamblue.cloud.common.util.ErrorCode;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
@Getter
public class HostUtil implements CommandLineRunner {


    private String clientId;
    private String clientSecret;
    private String managerUri;

    public void init(String managerUri, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managerUri = managerUri;
        Map<String, String> config = new HashMap<>(2);
        config.put("clientId", clientId);
        config.put("clientSecret", clientSecret);
        config.put("managerUri", managerUri);
        File configFile = new File("./config.json");
        FileUtil.writeUtf8String(GsonBuilderUtil.create().toJson(config), configFile);
    }

    private void init() throws Exception {
        File configFile = new File("./config.json");
        if (configFile.exists()) {
            Map<String, String> config = GsonBuilderUtil.create().fromJson(FileUtil.readUtf8String(configFile), new TypeToken<Map<String, Object>>() {
            }.getType());
            this.clientId = config.get("clientId");
            this.clientSecret = config.get("clientSecret");
            this.managerUri = config.get("managerUri");
        }
        if (!StringUtils.isEmpty(this.clientId) && !StringUtils.isEmpty(this.clientSecret) && !StringUtils.isEmpty(this.managerUri)) {
            String nonce = String.valueOf(System.nanoTime());
            Map<String, Object> map = new HashMap<>(2);
            map.put("clientId", this.clientId);
            map.put("nonce", nonce);
            String sign = AppUtils.sign(map, this.clientId, this.clientSecret, nonce);
            map.put("sign", sign);
            String response = HttpUtil.post(this.managerUri + "api/agent/register", map);
            ResultUtil<Void> result = GsonBuilderUtil.create().fromJson(response, new com.google.common.reflect.TypeToken<ResultUtil<Void>>() {
            }.getType());
            if (result.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "初始化失败:" + result.getMessage());
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        this.init();
    }
}
