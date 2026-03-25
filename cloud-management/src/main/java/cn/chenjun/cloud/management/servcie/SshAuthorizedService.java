package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.SshAuthorizedEntity;
import cn.chenjun.cloud.management.servcie.bean.MemSshInfo;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.OpenSSHPrivateKeyUtil;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class SshAuthorizedService extends AbstractService {
    @Autowired
    private RedissonClient redissonClient;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public List<SshAuthorizedEntity> listAllSshKeys() {
        List<SshAuthorizedEntity> list = this.sshAuthorizedDao.listAll();
        return list;
    }

    public Page<SshAuthorizedEntity> search(String keyword, int no, int size) {

        Page<SshAuthorizedEntity> page = this.sshAuthorizedDao.search(keyword, no, size);
        return page;
    }

    public SshAuthorizedEntity getSshKey(int id) {
        SshAuthorizedEntity entity = this.sshAuthorizedDao.findById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.SSH_AUTHORIZED_NOT_FOUND, "SSH公钥不存在");
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public SshAuthorizedEntity importSshKey(String name, String publicKey, String privateKey) {
        SshAuthorizedEntity entity = SshAuthorizedEntity.builder().sshName(name).sshPublicKey(publicKey).sshPrivateKey(privateKey).build();
        this.sshAuthorizedDao.insert(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH).build());

        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSshKey(int id) {
        this.sshAuthorizedDao.deleteById(id);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(id).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH).build());

    }

    @Transactional(rollbackFor = Exception.class)
    public SshAuthorizedEntity createSshKey(String name) {

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);
            java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
            AsymmetricKeyParameter privateKeyParam = org.bouncycastle.crypto.util.PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
            AsymmetricKeyParameter publicKeyParam = org.bouncycastle.crypto.util.PublicKeyFactory.createKey(keyPair.getPublic().getEncoded());
            byte[] privateKeyBuffer = OpenSSHPrivateKeyUtil.encodePrivateKey(privateKeyParam);
            byte[] publicKeyBuffer = OpenSSHPublicKeyUtil.encodePublicKey(publicKeyParam);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PemWriter pemWriter = new PemWriter(new OutputStreamWriter(outputStream));
                pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", privateKeyBuffer));
                pemWriter.close();
                String privateKey = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                String publicKey = "ssh-rsa " + Base64.getEncoder().encodeToString(publicKeyBuffer) + " cj-kvm-ssh-key";
                SshAuthorizedEntity entity = SshAuthorizedEntity.builder().sshName(name).sshPrivateKey(privateKey).sshPublicKey(publicKey).build();
                this.sshAuthorizedDao.insert(entity);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH).build());
                return entity;
            }

        } catch (Exception err) {
            log.error("SSH密钥生成失败", err);
            throw new CodeException(ErrorCode.SSH_AUTHORIZED_CREATE_ERROR, "SSH密钥生成失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public SshAuthorizedEntity modifySshKey(int id, String name) {
        SshAuthorizedEntity entity = this.sshAuthorizedDao.findById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.SSH_AUTHORIZED_NOT_FOUND, "SSH公钥不存在");
        }
        entity.setSshName(name);
        this.sshAuthorizedDao.update(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH).build());
        return entity;
    }

    public String createDownloadKey(int id) {
        SshAuthorizedEntity entity = this.sshAuthorizedDao.findById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.SSH_AUTHORIZED_NOT_FOUND, "SSH公钥不存在");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        RBucket<MemSshInfo> rBucket = redissonClient.getBucket("SSH." + token);

        rBucket.set(MemSshInfo.builder().id(id).name(entity.getSshName()).privateKey(entity.getSshPrivateKey()).publicKey(entity.getSshPublicKey()).build(), 10, TimeUnit.MINUTES);
        return token;
    }

    public MemSshInfo getDownloadKey(String token) {
        RBucket<MemSshInfo> rBucket = redissonClient.getBucket("SSH." + token);
        return rBucket.getAndDelete();
    }
}
