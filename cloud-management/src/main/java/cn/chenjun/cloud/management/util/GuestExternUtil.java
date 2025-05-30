package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.util.SymmetricCryptoUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class GuestExternUtil {
    private static final int VNC_PASSWORD_SIZE = 8;

    public static Map<String, String> buildMetaDataParam(GuestEntity guest, String hostname) {
        Map<String, String> metaDataMap = new HashMap<>(4);
        if (ObjectUtils.isEmpty(hostname)) {
            hostname = "VM-" + guest.getGuestIp().replace(".", "-");
        }
        metaDataMap.put(GuestExternNames.MetaDataNames.HOSTNAME, hostname);
        metaDataMap.put(GuestExternNames.MetaDataNames.LOCAL_HOSTNAME, hostname);
        metaDataMap.put(GuestExternNames.MetaDataNames.INSTANCE_ID, guest.getName());
        return metaDataMap;
    }

    public static Map<String, String> buildUserDataParam(GuestEntity guest, String password, String sshPublicKey) {
        Map<String, String> userDataMap = new HashMap<>(3);
        if (!StringUtils.isEmpty(password)) {
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build();
            userDataMap.put(GuestExternNames.UserDataNames.PASSWORD_IV_KEY, util.getIvKey());
            userDataMap.put(GuestExternNames.UserDataNames.PASSWORD_ENCODE_KEY, util.getEncodeKey());
            userDataMap.put(GuestExternNames.UserDataNames.PASSWORD, util.encrypt(password));
        }
        if (!StringUtils.isEmpty(sshPublicKey)) {
            userDataMap.put(GuestExternNames.UserDataNames.SSH_PUBLIC_KEY, sshPublicKey);
        }
        return userDataMap;
    }

    public static Map<String, String> buildVncParam(GuestEntity guest, String host, String port) {
        Map<String, String> vncMap = new HashMap<>(1);
        vncMap.put(GuestExternNames.VncNames.PORT, port);
        vncMap.put(GuestExternNames.VncNames.HOST, host);
        vncMap.put(GuestExternNames.VncNames.PASSWORD, RandomStringUtils.randomAlphanumeric(VNC_PASSWORD_SIZE));
        return vncMap;
    }
}
