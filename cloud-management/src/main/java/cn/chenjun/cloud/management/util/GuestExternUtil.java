package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.util.SymmetricCryptoUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GuestExternUtil {
    private static final int VNC_PASSWORD_SIZE = 8;

    public static GuestExtern.MetaData buildMetaDataParam(GuestEntity guest, String hostname) {
        GuestExtern.MetaData metaData = new GuestExtern.MetaData();
        if (ObjectUtils.isEmpty(hostname)) {
            hostname = "VM-" + guest.getGuestIp().replace(".", "-");
        }
        metaData.setHostname(hostname);
        metaData.setLocalHostname(hostname);
        metaData.setInstanceId(guest.getName());
        return metaData;
    }

    public static  GuestExtern.UserData buildUserDataParam(GuestEntity guest, String password, String sshPublicKey) {
        GuestExtern.UserData userData = new GuestExtern.UserData();
        if (!StringUtils.isEmpty(password)) {
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build();
            userData.setPasswordIvKey(util.getIvKey());
            userData.setPasswordEncodeKey(util.getEncodeKey());
            userData.setPassword(util.encrypt(password));
        }
        userData.setSshPublicKey(sshPublicKey);
        return userData;
    }

    public static GuestExtern.Vnc buildVncParam(GuestEntity guest, String host, String port) {
        GuestExtern.Vnc vnc = new GuestExtern.Vnc();
        vnc.setHost(host);
        vnc.setPort(port);
        vnc.setPassword(RandomStringUtils.randomAlphanumeric(VNC_PASSWORD_SIZE));
        return vnc;
    }
}
