package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
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


    public MetaData loadAllGuestMetaData(int networkId, String ip, String nonce, String sign) {
        do {
            GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
            if (guestNetwork == null) {
                break;
            }
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            if (network == null) {
                break;
            }
            if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
                break;
            }
            GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
            if (guest == null) {
                break;
            }
            Optional<MetaDataService> metaDataServiceOptional = metaDataPluginRegistry.getPluginFor(guest.getSystemCategory());
            if(metaDataServiceOptional.isPresent()){
                MetaData metaData = metaDataServiceOptional.get().buildCloudInitMetaData(guest.getGuestId());
                return metaData;
            }
        } while (false);
        return null;
    }

    public String findMetaDataByKey(String key, int networkId, String ip, String nonce, String sign) {
        StringBuilder data = new StringBuilder();
        do {
            GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
            if (guestNetwork == null) {
                break;
            }
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            if (network == null) {
                break;
            }
            if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
                break;
            }
            GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
            if (guest == null) {
                break;
            }
            Optional<MetaDataService> optional = metaDataPluginRegistry.getPluginFor(guest.getSystemCategory());
            if (!optional.isPresent()) {
                break;
            }
            String metaData = optional.get().findMetaDataByKey(guest.getGuestId(), key);
            if (!ObjectUtils.isEmpty(metaData)) {
                data.append(metaData).append("\n");
            }
        } while (false);
        return data.toString();
    }

    public List<MetaData> findGuestVendorData(int networkId, String ip, String nonce, String sign) {
        List<MetaData> metaDataList = new ArrayList<>();
        do {
            GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
            if (guestNetwork == null) {
                break;
            }
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            if (network == null) {
                break;
            }
            if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
                break;
            }
            GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
            if (guest == null) {
                break;
            }
            List<VendorDataService> vendorDataServiceList = vendorDataPluginRegistry.getPluginsFor(guest.getSystemCategory());
            for (VendorDataService vendorDataService : vendorDataServiceList) {
                MetaData metaData = vendorDataService.load(guest.getGuestId());
                if (!ObjectUtils.isEmpty(metaData.getBody())) {
                    metaDataList.add(metaData);
                }
            }
        } while (false);
        return metaDataList;
    }

    public List<MetaData> findGuestInitData(int networkId, String ip, String nonce, String sign) {
        List<MetaData> metaDataList = new ArrayList<>();
        do {
            GuestNetworkEntity guestNetwork = guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.NETWORK_IP, ip).eq(GuestNetworkEntity.NETWORK_ID, networkId));
            if (guestNetwork == null) {
                break;
            }
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            if (network == null) {
                break;
            }
            if (!DigestUtil.md5Hex(network.getSecret() + ":" + nonce + ":" + ip).equals(sign)) {
                break;
            }
            GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
            if (guest == null) {
                break;
            }
            List<UserDataService> userDataServiceList = userDataPluginRegistry.getPluginsFor(guest.getSystemCategory());
            for (UserDataService userDataService : userDataServiceList) {
                MetaData metaData = userDataService.load(guest.getGuestId());
                if (!ObjectUtils.isEmpty(metaData.getBody())) {
                    metaDataList.add(metaData);
                }
            }
        } while (false);
        return metaDataList;
    }


}
