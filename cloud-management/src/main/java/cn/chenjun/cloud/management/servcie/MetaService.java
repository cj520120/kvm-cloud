package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.servcie.meta.UserDataService;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
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
    private PluginRegistry<MetaDataService, GuestEntity> metaDataPluginRegistry;
    @Autowired
    private PluginRegistry<UserDataService, GuestEntity> userDataPluginRegistry;
    @Autowired
    private PluginRegistry<VendorDataService, GuestEntity> vendorDataPluginRegistry;


    public MetaData loadAllGuestMetaData(int networkId, String ip) {
        do {
            GuestEntity guest = getRequestGuest(networkId, ip);
            if (guest == null) break;
            Optional<MetaDataService> metaDataServiceOptional = metaDataPluginRegistry.getPluginFor(guest);
            if (metaDataServiceOptional.isPresent()) {
                MetaData metaData = metaDataServiceOptional.get().buildCloudInitMetaData(guest);
                return metaData;
            }
        } while (false);
        return null;
    }

    public String listMetaDataKeys(int networkId, String ip) {
        StringBuilder data = new StringBuilder();
        do {
            GuestEntity guest = getRequestGuest(networkId, ip);
            if (guest == null) break;
            Optional<MetaDataService> optional = metaDataPluginRegistry.getPluginFor(guest);
            if (!optional.isPresent()) {
                break;
            }
            List<String> metaKeys = optional.get().listMetaDataKeys(guest);
            data.append(String.join("\n", metaKeys));
        } while (false);
        return data.toString();
    }

    public String findMetaDataByKey(String key, int networkId, String ip) {
        StringBuilder data = new StringBuilder();
        do {
            GuestNetworkEntity guestNetwork = guestNetworkDao.findByIp(networkId, ip);
            if (guestNetwork == null) {
                break;
            }
            NetworkEntity network = networkDao.findById(guestNetwork.getNetworkId());
            if (network == null) {
                break;
            }
            GuestEntity guest = guestDao.findById(guestNetwork.getAllocateId());
            if (guest == null) {
                break;
            }
            Optional<MetaDataService> optional = metaDataPluginRegistry.getPluginFor(guest);
            if (!optional.isPresent()) {
                break;
            }
            String metaData = optional.get().findMetaDataByKey(guest, key);
            if (!ObjectUtils.isEmpty(metaData)) {
                data.append(metaData).append("\n");
            }
        } while (false);
        return data.toString();
    }

    public List<MetaData> findGuestVendorData(int networkId, String ip) {
        List<MetaData> metaDataList = new ArrayList<>();
        do {
            GuestEntity guest = getRequestGuest(networkId, ip);
            if (guest == null) break;
            List<VendorDataService> vendorDataServiceList = vendorDataPluginRegistry.getPluginsFor(guest);
            for (VendorDataService vendorDataService : vendorDataServiceList) {
                MetaData metaData = vendorDataService.load(guest);
                if (metaData != null && !ObjectUtils.isEmpty(metaData.getBody())) {
                    metaDataList.add(metaData);
                }
            }
        } while (false);
        return metaDataList;
    }

    public List<MetaData> findGuestUserData(int networkId, String ip) {
        List<MetaData> metaDataList = new ArrayList<>();
        do {
            GuestEntity guest = getRequestGuest(networkId, ip);
            if (guest == null) break;
            List<UserDataService> userDataServiceList = userDataPluginRegistry.getPluginsFor(guest);
            for (UserDataService userDataService : userDataServiceList) {
                MetaData metaData = userDataService.load(guest);
                if (metaData != null && !ObjectUtils.isEmpty(metaData.getBody())) {
                    metaDataList.add(metaData);
                }
            }
        } while (false);
        return metaDataList;
    }

    private GuestEntity getRequestGuest(int networkId, String ip) {
        GuestNetworkEntity guestNetwork = guestNetworkDao.findByIp(networkId, ip);
        if (guestNetwork == null) {
            return null;
        }
        NetworkEntity network = networkDao.findById(guestNetwork.getNetworkId());
        if (network == null) {
            return null;
        }
        GuestEntity guest = guestDao.findById(guestNetwork.getAllocateId());
        return guest;
    }


}
