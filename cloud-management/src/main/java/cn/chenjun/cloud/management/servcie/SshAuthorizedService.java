package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.SshAuthorizedEntity;
import cn.chenjun.cloud.management.model.CreateSshAuthorizedModel;
import cn.chenjun.cloud.management.model.SshAuthorizedModel;
import cn.chenjun.cloud.management.servcie.bean.MemSshInfo;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class SshAuthorizedService extends AbstractService {
    @Autowired
    private RedissonClient redissonClient;

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<SshAuthorizedModel>> listAllSshKeys() {
        List<SshAuthorizedEntity> list = this.sshAuthorizedMapper.selectList(new QueryWrapper<>());
        List<SshAuthorizedModel> models = list.stream().map(this::initSshAuthorized).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SshAuthorizedModel> getSshKey(int id) {
        SshAuthorizedEntity entity = this.sshAuthorizedMapper.selectById(id);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.SSH_AUTHORIZED_NOT_FOUND, "SSH公钥不存在");
        }
        return ResultUtil.success(this.initSshAuthorized(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SshAuthorizedModel> importSshKey(String name, String publicKey, String privateKey) {
        SshAuthorizedEntity entity = SshAuthorizedEntity.builder().sshName(name).sshPublicKey(publicKey).build();
        this.sshAuthorizedMapper.insert(entity);
        this.eventService.publish(NotifyData.<Void>builder().id(entity.getId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH_KEY).build());

        return ResultUtil.success(this.initSshAuthorized(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> deleteSshKey(int id) {
        this.sshAuthorizedMapper.deleteById(id);
        this.eventService.publish(NotifyData.<Void>builder().id(id).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH_KEY).build());
        return ResultUtil.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<CreateSshAuthorizedModel> createSshKey(String name) {
        JSch jsch = new JSch();
        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            CreateSshAuthorizedModel model = CreateSshAuthorizedModel.builder().build();
            SshAuthorizedEntity entity = SshAuthorizedEntity.builder().sshName(name).build();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                keyPair.writePublicKey(outputStream, "CJ-KVM");
                String publicKey = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                model.setPublicKey(publicKey);
                model.setId(entity.getId());
                model.setPublicKey(publicKey);
                model.setName(name);
                entity.setSshPublicKey(publicKey);
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                keyPair.writePrivateKey(outputStream);
                String privateKey = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                model.setPrivateKey(privateKey);
                entity.setSshPrivateKey(privateKey);
            }
            this.sshAuthorizedMapper.insert(entity);
            model.setId(entity.getId());
            this.eventService.publish(NotifyData.<Void>builder().id(entity.getId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH_KEY).build());

            return ResultUtil.success(model);
        } catch (Exception err) {
            log.error("SSH密钥生成失败", err);
            return ResultUtil.error(ErrorCode.SSH_AUTHORIZED_CREATE_ERROR, "SSH密钥生成失败");
        }
    }

    public ResultUtil<String> createDownloadKey(int id) {
        SshAuthorizedEntity entity = this.sshAuthorizedMapper.selectById(id);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.SSH_AUTHORIZED_NOT_FOUND, "SSH公钥不存在");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        RBucket<MemSshInfo> rBucket = redissonClient.getBucket("SSH." + token);

        rBucket.set(MemSshInfo.builder().id(id).name(entity.getSshName()).privateKey(entity.getSshPrivateKey()).publicKey(entity.getSshPublicKey()).build(), 10, TimeUnit.MINUTES);
        return ResultUtil.success(token);
    }

    public MemSshInfo getDownloadKey(String token) {

        RBucket<MemSshInfo> rBucket = redissonClient.getBucket("SSH." + token);
        return rBucket.getAndDelete();
    }
}
