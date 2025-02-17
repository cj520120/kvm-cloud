package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestPasswordEntity;
import cn.chenjun.cloud.management.data.mapper.GuestPasswordMapper;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
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
            data.append("admin_pass: ").append(password).append("\n");
        } while (false);


        return MetaData.builder().type(MetaDataType.CLOUD).body(data.toString()).build();
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() == SystemCategory.WINDOWS;
    }
}
