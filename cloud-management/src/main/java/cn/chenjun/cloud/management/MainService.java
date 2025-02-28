package cn.chenjun.cloud.management;

import cn.chenjun.cloud.management.component.ComponentProcess;
import cn.chenjun.cloud.management.operate.Operate;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import cn.chenjun.cloud.management.websocket.cluster.process.ClusterMessageProcess;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author chenjun
 */
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
@EnablePluginRegistries({ClusterMessageProcess.class, Operate.class, ComponentProcess.class, VendorDataService.class, MetaDataService.class, UserDataService.class})
@MapperScan("cn.chenjun.cloud.management.data")
public class MainService {
    public static void main(String[] args) {
//        String tpl = "{{__SYS__['a.d']}}";
//        Map<String, Map> map = new HashMap<>();
//        map.put("__SYS__", MapUtil.of("a.d", 123));
//        System.out.println(TemplateUtil.create().render(tpl, map));
        SpringApplication.run(MainService.class, args);
    }
}
