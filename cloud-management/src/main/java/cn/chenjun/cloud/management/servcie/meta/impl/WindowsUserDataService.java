package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.GuestPasswordEntity;
import cn.chenjun.cloud.management.data.mapper.GuestPasswordMapper;
import cn.chenjun.cloud.management.data.mapper.GuestSshMapper;
import cn.chenjun.cloud.management.data.mapper.SshAuthorizedMapper;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.util.SymmetricCryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Service
public class WindowsUserDataService implements UserDataService {
    @Autowired
    protected GuestSshMapper guestSshMapper;
    @Autowired
    protected SshAuthorizedMapper sshAuthorizedMapper;
    @Autowired
    private GuestPasswordMapper guestPasswordMapper;

    @Override
    public String loadUserData(int guestId) {
        StringBuilder data = new StringBuilder();
        do {
            GuestPasswordEntity entity = guestPasswordMapper.selectById(guestId);
            if (entity == null) {
                break;
            }
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build(entity.getEncodeKey(), entity.getIvKey());
            String password = util.decrypt(entity.getPassword());
            if (StringUtils.isEmpty(password)) {
                break;
            }
            data.append("admin_pass: ").append(password).append("\n");
        } while (false);


        return data.toString();
    }

    @Override
    public boolean supports(@NonNull Integer systemCategory) {
        return systemCategory == SystemCategory.WINDOWS;
    }
}
