package cn.roamblue.cloud.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author chenjun
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("cn.roamblue.cloud.management.data")
public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class, args);
    }
}
