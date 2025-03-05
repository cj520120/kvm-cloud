package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        File runPath=new File("/var/run/cj-kvm-cloud");
        if(!runPath.exists()){
            runPath.mkdirs();
        }
        boolean isWrite;
        byte[] buffer=ResourceUtil.readBytes("cloud/local-cloud.img");
        File cloudFile=new File("/var/run/cj-kvm-cloud/local-cloud.img");
        if(!cloudFile.exists()){
            isWrite=true;
        }else{
            byte[] oldBuffer=FileUtil.readBytes(cloudFile);
            isWrite= !Objects.equals(MD5.create().digest(buffer),MD5.create().digest(oldBuffer));
        }
        if(isWrite){
            FileUtil.writeBytes(buffer,cloudFile);
        }

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
            map.put("timestamp", System.currentTimeMillis());
            String sign = AppUtils.sign(map, this.clientId, this.clientSecret, nonce);
            map.put("sign", sign);
            String response = HttpUtil.post(this.managerUri + "api/agent/register", map);
            ResultUtil<Void> result = GsonBuilderUtil.create().fromJson(response, new com.google.gson.reflect.TypeToken<ResultUtil<Void>>() {
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
