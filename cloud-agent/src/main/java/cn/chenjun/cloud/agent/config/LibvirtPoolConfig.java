package cn.chenjun.cloud.agent.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.libvirt.Connect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenjun
 */
@Configuration
public class LibvirtPoolConfig {

    @Bean
    public GenericObjectPoolConfig<Connect> genericObjectPoolConfig(ApplicationConfig applicationConfig) {
        GenericObjectPoolConfig<Connect> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(Math.max(applicationConfig.getTaskThreadSize(),1));
        poolConfig.setMinIdle(1);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(18000L);
        poolConfig.setJmxEnabled(false);
        return poolConfig;
    }
}
