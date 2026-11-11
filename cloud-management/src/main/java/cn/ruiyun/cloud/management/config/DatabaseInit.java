package cn.ruiyun.cloud.management.config;

import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.SchemeEntity;
import cn.ruiyun.cloud.management.data.mapper.SchemeMapper;
import cn.ruiyun.cloud.management.data.mapper.UserInfoMapper;
import cn.ruiyun.cloud.management.servcie.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Configuration
public class DatabaseInit implements ApplicationRunner {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private SchemeMapper schemeMapper;
    @Autowired
    private UserService userService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        long count = userInfoMapper.selectCount(new QueryWrapper<>());
        if (count == 0) {
            userService.register("管理员", "admin", "123456", Constant.UserType.SUPPER_ADMIN, Constant.UserState.ABLE);
        }
        count = schemeMapper.selectCount(new QueryWrapper<>());
        if (count == 0) {
            List<SchemeEntity> list = Arrays.asList(
                    SchemeEntity.builder().name("1核512MB").cpu(1).memory(524288L).share(0).sockets(0).cores(0).threads(0).createTime(new Date()).build(),
                    SchemeEntity.builder().name("1核1GB").cpu(1).memory(1048576L).share(0).sockets(0).cores(0).threads(0).createTime(new Date()).build(),
                    SchemeEntity.builder().name("1核2GB").cpu(1).memory(2097152L).share(0).sockets(0).cores(0).threads(0).createTime(new Date()).build(),
                    SchemeEntity.builder().name("1核4GB").cpu(1).memory(4194304L).share(0).sockets(0).cores(0).threads(0).createTime(new Date()).build(),
                    SchemeEntity.builder().name("1核8GB").cpu(1).memory(8388608L).share(0).sockets(0).cores(0).threads(0).createTime(new Date()).build(),
                    SchemeEntity.builder().name("2核1GB").cpu(2).memory(1048576L).share(0).sockets(1).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("2核2GB").cpu(2).memory(2097152L).share(0).sockets(1).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("2核4GB").cpu(2).memory(4194304L).share(0).sockets(1).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("2核8GB").cpu(2).memory(8388608L).share(0).sockets(1).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("2核16GB").cpu(2).memory(16777216L).share(0).sockets(1).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("4核4G").cpu(4).memory(4194304L).share(0).sockets(2).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("4核8G").cpu(4).memory(8388608L).share(0).sockets(2).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("4核16G").cpu(4).memory(16777216L).share(0).sockets(2).cores(1).threads(2).createTime(new Date()).build(),
                    SchemeEntity.builder().name("8核16G").cpu(8).memory(16777216L).share(0).sockets(0).cores(0).threads(0).createTime(new Date()).build()
            );

            for (SchemeEntity scheme : list) {
                schemeMapper.insert(scheme);
            }
        }
    }
}
