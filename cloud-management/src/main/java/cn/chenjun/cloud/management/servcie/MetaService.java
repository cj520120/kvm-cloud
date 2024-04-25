package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class MetaService extends AbstractService {
    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private PluginRegistry<MetaDataService, Integer> metaDataPluginRegistry;
    @Autowired
    private PluginRegistry<UserDataService, Integer> userDataPluginRegistry;
    @Autowired
    private PluginRegistry<VendorDataService, Integer> vendorDataPluginRegistry;


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
        GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
        if (guest == null) {
            return "";
        }
        Optional<MetaDataService> optional = metaDataPluginRegistry.getPluginFor(guest.getSystemCategory());
        if (!optional.isPresent()) {
            return "";
        }
        return optional.get().loadMetaData(guest.getGuestId());
    }

    public String findGuestVendorData(int networkId, String ip, String nonce, String sign) {

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
        GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
        if (guest == null) {
            return data.toString();
        }
        Optional<VendorDataService> optional = vendorDataPluginRegistry.getPluginFor(guest.getSystemCategory());
        if (!optional.isPresent()) {
            return data.toString();
        }
        VendorDataService vendorDataService = optional.get();
        data.append(vendorDataService.loadVendorData(guest.getGuestId()));
        return data.toString();
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
        GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
        if (guest == null) {
            return data.toString();
        }
        Optional<UserDataService> optional = userDataPluginRegistry.getPluginFor(guest.getSystemCategory());
        if (!optional.isPresent()) {
            return data.toString();
        }
        UserDataService userDataService = optional.get();
        data.append(userDataService.loadUserData(guest.getGuestId()));
        return data.toString();

    }
}
