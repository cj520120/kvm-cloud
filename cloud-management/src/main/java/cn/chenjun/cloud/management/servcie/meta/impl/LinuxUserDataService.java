package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestPasswordEntity;
import cn.chenjun.cloud.management.data.entity.GuestSshEntity;
import cn.chenjun.cloud.management.data.entity.SshAuthorizedEntity;
import cn.chenjun.cloud.management.data.mapper.GuestPasswordMapper;
import cn.chenjun.cloud.management.data.mapper.GuestSshMapper;
import cn.chenjun.cloud.management.data.mapper.SshAuthorizedMapper;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import cn.chenjun.cloud.management.util.SymmetricCryptoUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Service
public class LinuxUserDataService implements UserDataService {
    @Autowired
    protected GuestSshMapper guestSshMapper;
    @Autowired
    protected SshAuthorizedMapper sshAuthorizedMapper;
    @Autowired
    private GuestPasswordMapper guestPasswordMapper;

    @Override
    public MetaData load(GuestEntity guest) {
        StringBuilder data = new StringBuilder();
        do {
            GuestPasswordEntity entity = guestPasswordMapper.selectById(guest.getGuestId());
            if (entity == null) {
                break;
            }
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build(entity.getEncodeKey(), entity.getIvKey());
            String password = util.decrypt(entity.getPassword());
            if (StringUtils.isEmpty(password)) {
                break;
            }

            data.append("password: \"").append(password).append("\"\n");
            data.append("chpasswd: {expire: False}\n");
            data.append("ssh_pwauth: True\n");
        } while (false);

        do {
            GuestSshEntity guestSshEntity = this.guestSshMapper.selectOne(new QueryWrapper<GuestSshEntity>().eq(GuestSshEntity.GUEST_ID, guest.getGuestId()));
            if (guestSshEntity == null) {
                break;
            }
            if (guestSshEntity.getSshId() <= 0) {
                break;
            }
            SshAuthorizedEntity sshAuthorizedEntity = this.sshAuthorizedMapper.selectById(guestSshEntity.getSshId());
            if (sshAuthorizedEntity == null) {
                break;
            }
            if (ObjectUtils.isEmpty(sshAuthorizedEntity.getSshPublicKey())) {
                break;
            }
            data.append("ssh_authorized_keys:\n");
            data.append("  - ").append(sshAuthorizedEntity.getSshPublicKey());
            data.append("\n");
        } while (false);
        return MetaData.builder().type(MetaDataType.CLOUD).body(data.toString()).build();
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() != SystemCategory.WINDOWS;
    }
}
