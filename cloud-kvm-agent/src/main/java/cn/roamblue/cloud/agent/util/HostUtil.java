package cn.roamblue.cloud.agent.util;

import cn.hutool.core.io.FileUtil;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.AppUtils;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenjun
 */
@Getter
public class HostUtil implements CommandLineRunner {


    private String clientId;
    private String clientSecret;

    public void init(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        Map<String,String> config=new HashMap<>(2);
        config.put("clientId",clientId);
        config.put("clientSecret",clientSecret);
        FileUtil.writeUtf8String(GsonBuilderUtil.create().toJson(config), "config.json");
    }
    private void init() throws Exception{
        File file=new File("config.json");
        if(file.exists()){
            Map<String,String> config= GsonBuilderUtil.create().fromJson(FileUtil.readUtf8String(file),new TypeToken<Map<String,Object>>(){}.getType());
            this.clientId= config.get("clientId");
            this.clientSecret= config.get("clientSecret");
        }
    }

    @Override
    public void run(String... args) throws Exception {
        this.init();
    }
}
