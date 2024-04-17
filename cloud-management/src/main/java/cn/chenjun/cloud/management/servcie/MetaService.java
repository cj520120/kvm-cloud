package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.GuestPasswordMapper;
import cn.chenjun.cloud.management.data.mapper.MetaMapper;
import cn.chenjun.cloud.management.util.SymmetricCryptoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class MetaService extends AbstractService {
    @Autowired
    private MetaMapper metaMapper;
    @Autowired
    private GuestPasswordMapper guestPasswordMapper;


    public String loadAllGuestMetaData(int networkId, String ip, String nonce, String sign) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
        if (guestNetwork == null) {
            return "";
        }
        NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
        if (network == null) {
            return "";
        }
        if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
            return "";
        }
        List<MetaDataEntity> list = this.metaMapper.selectList(new QueryWrapper<MetaDataEntity>().eq(MetaDataEntity.GUEST_ID, guestNetwork.getAllocateId()));
        Set<String> metaNames = list.stream().map(t -> t.getMetaKey() + ": " + t.getMetaValue()).collect(Collectors.toSet());
        return String.join("\r\n", metaNames);
    }

    public String findGuestVendorData(int networkId, String ip, String nonce, String sign) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
        if (guestNetwork == null) {
            return "";
        }
        NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
        if (network == null) {
            return "";
        }
        if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
            return "";
        }
        return "#cloud-config\nbootcmd:\n - echo ----------complete-------------";
    }

    public String loadAllGuestUserData(int networkId, String ip, String nonce, String sign) {
        StringBuilder data = new StringBuilder("#cloud-config\n");
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
        if (guestNetwork == null) {
            return data.toString();
        }
        NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
        if (network == null) {
            return data.toString();
        }
        if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
            return data.toString();
        }
        do{
            GuestPasswordEntity entity = guestPasswordMapper.selectById(guestNetwork.getAllocateId());
            if (entity == null) {
                break;
            }
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build(entity.getEncodeKey(), entity.getIvKey());
            String password = util.decrypt(entity.getPassword());
            if (StringUtils.isEmpty(password)) {
                break;
            }

            data.append("password: ").append(password).append("\n");
            data.append("chpasswd: {expire: False}\n");
            data.append("ssh_pwauth: True\n");
        }while(false);

        do {
            GuestSshEntity guestSshEntity = this.guestSshMapper.selectOne(new QueryWrapper<GuestSshEntity>().eq(GuestSshEntity.GUEST_ID, guestNetwork.getAllocateId()));
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
            if(ObjectUtils.isEmpty(sshAuthorizedEntity.getSshKey())){
                break;
            }
            data.append("ssh_authorized_keys:\n");
            data.append("  - ").append(sshAuthorizedEntity.getSshKey());
            data.append("\n");
        } while (false);
        return data.toString();
    }
}
