package cn.ruiyun.cloud.management;

import cn.ruiyun.cloud.common.core.operate.OperateService;
import cn.ruiyun.cloud.management.data.mapper.ConfigMapper;
import cn.ruiyun.cloud.management.servcie.meta.MetaDataService;
import cn.ruiyun.cloud.management.servcie.meta.UserDataService;
import cn.ruiyun.cloud.management.servcie.meta.VendorDataService;
import cn.ruiyun.cloud.management.websocket.cluster.process.ClusterMessageProcess;
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
@EnablePluginRegistries({ClusterMessageProcess.class, OperateService.class, VendorDataService.class, MetaDataService.class, UserDataService.class})
@MapperScan(basePackageClasses = ConfigMapper.class)
public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class, args);
    }
}
