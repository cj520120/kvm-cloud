package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
@Getter
public class ClientService implements CommandLineRunner {


    private String clientId;
    private String clientSecret;
    private String managerUri;

    public ResultUtil<Void> init(String managerUri, String clientId, String clientSecret) {
        if (!StringUtils.isEmpty(this.clientId) && !this.clientId.equalsIgnoreCase(clientId)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "节点已经被添加到其他集群");
        }
        if (!StringUtils.isEmpty(this.clientSecret) && !this.clientSecret.equalsIgnoreCase(clientSecret)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "节点已经被添加到其他集群");
        }
        if (!managerUri.matches("^https?://.*$")) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "管理地址不合法");
        }
        if (!managerUri.endsWith("/")) {
            managerUri += "/";
        }
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managerUri = managerUri;
        Map<String, String> config = new HashMap<>(2);
        config.put("clientId", clientId);
        config.put("clientSecret", clientSecret);
        config.put("managerUri", managerUri);
        File configFile = new File("./config.json");
        FileUtil.writeUtf8String(GsonBuilderUtil.create().toJson(config), configFile);
        return ResultUtil.success();
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
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getManagerUri() {
        return managerUri;
    }

    @Override
    public void run(String... args) throws Exception {
        this.init();
    }

    public void updateManagerUri(String url) {
        if (!ObjectUtils.isEmpty(url)) {
            this.init(url, this.clientId, this.clientSecret);
        }
    }

    public boolean isInit() {
        return !ObjectUtils.isEmpty(this.clientId) && !ObjectUtils.isEmpty(this.clientSecret) && !ObjectUtils.isEmpty(this.managerUri);
    }
}
