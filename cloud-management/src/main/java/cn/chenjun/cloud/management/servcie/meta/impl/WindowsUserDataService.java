package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.SymmetricCryptoUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.util.GuestExternNames;
import cn.chenjun.cloud.management.util.MetaDataType;
import com.google.gson.reflect.TypeToken;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author chenjun
 */
@Service
public class WindowsUserDataService implements UserDataService {

    @Override
    public MetaData load(GuestEntity guest) {
        Map<String, Map<String, String>> externMap = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
        StringBuilder data = new StringBuilder();
        Map<String, String> userMap = externMap.getOrDefault(GuestExternNames.USER_DATA, null);
        if (userMap == null) {
            return MetaData.builder().type(MetaDataType.CLOUD).body(data.toString()).build();
        }
        String encodeKey = userMap.get(GuestExternNames.UserDataNames.PASSWORD_ENCODE_KEY);
        String ivKey = userMap.get(GuestExternNames.UserDataNames.PASSWORD_IV_KEY);
        String password = userMap.get(GuestExternNames.UserDataNames.PASSWORD);
        String ssh = userMap.get(GuestExternNames.UserDataNames.SSH_PUBLIC_KEY);
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
