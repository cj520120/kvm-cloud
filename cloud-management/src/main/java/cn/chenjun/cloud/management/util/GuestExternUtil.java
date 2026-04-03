package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.util.SymmetricCryptoUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

@Slf4j
public class GuestExternUtil {
    private static final int VNC_PASSWORD_SIZE = 8;

    public static GuestExtern.MetaDataExtern buildMetaDataParam(GuestEntity guest, String hostname) {
        GuestExtern.MetaDataExtern metaDataExtern = new GuestExtern.MetaDataExtern();
        if (ObjectUtils.isEmpty(hostname)) {
            hostname = "VM-" + guest.getGuestIp().replace(".", "-");
        }
        metaDataExtern.setHostname(hostname);
        metaDataExtern.setLocalHostname(hostname);
        metaDataExtern.setInstanceId(guest.getName());
        return metaDataExtern;
    }

    public static GuestExtern.UserDataExtern buildUserDataParam(GuestEntity guest, String password, String sshPublicKey) {
        GuestExtern.UserDataExtern userDataExtern = new GuestExtern.UserDataExtern();
        if (!StringUtils.isEmpty(password)) {
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build();
            userDataExtern.setPasswordIvKey(util.getIvKey());
            userDataExtern.setPasswordEncodeKey(util.getEncodeKey());
            userDataExtern.setPassword(util.encrypt(password));
        }
        userDataExtern.setSshPublicKey(sshPublicKey);
        return userDataExtern;
    }

    public static GuestExtern.GraphicsExtern buildVncParam() {
        GuestExtern.GraphicsExtern graphicsExtern = new GuestExtern.GraphicsExtern();
        graphicsExtern.setProtocol("vnc");
        graphicsExtern.setPassword(RandomStringUtils.randomAlphanumeric(VNC_PASSWORD_SIZE));
        return graphicsExtern;
    }
}
