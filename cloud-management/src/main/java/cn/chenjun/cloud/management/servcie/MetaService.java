package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.GuestPasswordEntity;
import cn.chenjun.cloud.management.data.entity.MetaDataEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.util.SymmetricCryptoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class MetaService {
    @Autowired
    private MetaMapper mapper;

    @Autowired
    private GuestMapper guestMapper;

    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private GuestPasswordMapper guestPasswordMapper;


    public String loadAllGuestMetaData(String ip, String sign) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("network_ip", ip));
        if (guestNetwork == null) {
            return "";
        }
        NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
        if (network == null) {
            return "";
        }
        if (!DigestUtil.md5Hex(network.getSecret() + ":" + ip).equals(sign)) {
            return "";
        }
        List<MetaDataEntity> list = mapper.selectList(new QueryWrapper<MetaDataEntity>().eq("guest_id", guestNetwork.getGuestId()));
        Set<String> metaNames = list.stream().map(t -> t.getMetaKey() + ": " + t.getMetaValue()).collect(Collectors.toSet());
        return String.join("\r\n", metaNames);
    }

    public String loadAllGuestUserData(String ip, String sign) {
        String data = "#cloud-config\r\n";
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("network_ip", ip));
        if (guestNetwork == null) {
            return data;
        }
        NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
        if (network == null) {
            return data;
        }
        if (!DigestUtil.md5Hex(network.getSecret() + ":" + ip).equals(sign)) {
            return data;
        }
        GuestPasswordEntity entity = guestPasswordMapper.selectById(guestNetwork.getGuestId());
        if (entity == null) {
            return data;
        }
        SymmetricCryptoUtil util = SymmetricCryptoUtil.build(entity.getEncodeKey(), entity.getIvKey());
        String password = util.decrypt(entity.getPassword());
        if (!StringUtils.isEmpty(password)) {

            data += "password: "+password+"\r\n";
            data += "chpasswd: {expire: False}\r\n";
            data += "ssh_pwauth: True";
        }
        return data;
    }
}
