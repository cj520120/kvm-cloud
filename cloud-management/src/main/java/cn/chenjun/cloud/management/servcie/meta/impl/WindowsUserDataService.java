package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.SymmetricCryptoUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
@Service
public class WindowsUserDataService implements UserDataService {

    @Override
    public MetaData load(GuestEntity guest) {
        GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
        if (extern == null || extern.getUserData() == null) {
            return MetaData.builder().type(MetaDataType.CLOUD).body("").build();
        }
        StringBuilder data = new StringBuilder();
        GuestExtern.UserData userData = extern.getUserData();
        String encodeKey = userData.getPasswordEncodeKey();
        String ivKey = userData.getPasswordIvKey();
        String password =userData.getPassword();
        if (!ObjectUtils.isEmpty(encodeKey) && !ObjectUtils.isEmpty(ivKey) && !ObjectUtils.isEmpty(password)) {
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build(encodeKey, ivKey);
            password = util.decrypt(password);
            if (!StringUtils.isEmpty(password)) {
                data.append("admin_pass: ").append(password).append("\n");
            }
        }
        return MetaData.builder().type(MetaDataType.CLOUD).body(data.toString()).build();
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() == Constant.SystemCategory.WINDOWS;
    }
}
