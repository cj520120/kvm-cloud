package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjun
 */
@Service
public class DefaultMetaDataService implements MetaDataService {


    @Override
    public MetaData buildCloudInitMetaData(GuestEntity guest) {
        GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
        List<String> metaDatas = new ArrayList<>();
        if (extern != null && extern.getMetaData() != null) {
            GuestExtern.MetaData metaData = extern.getMetaData();
            if (!ObjectUtils.isEmpty(metaData.getHostname())) {
                metaDatas.add(GuestExtern.GuestExternNames.MetaDataNames.HOSTNAME + ": " + metaData.getHostname());
            }
            if (!ObjectUtils.isEmpty(metaData.getLocalHostname())) {
                metaDatas.add(GuestExtern.GuestExternNames.MetaDataNames.LOCAL_HOSTNAME + ": " + metaData.getLocalHostname());
            }
            if (!ObjectUtils.isEmpty(metaData.getInstanceId())) {
                metaDatas.add(GuestExtern.GuestExternNames.MetaDataNames.INSTANCE_ID + ": " + metaData.getInstanceId());
            }
        }
        return MetaData.builder().type(MetaDataType.CLOUD).body(String.join("\r\n", metaDatas)).build();
    }

    @Override
    public String findMetaDataByKey(GuestEntity guest, String key) {
        GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
        if (extern == null || extern.getMetaData() == null) {
            return "";
        }
        if (GuestExtern.GuestExternNames.MetaDataNames.INSTANCE_ID.equals(key)) {
            return extern.getMetaData().getInstanceId();
        }
        if (GuestExtern.GuestExternNames.MetaDataNames.LOCAL_HOSTNAME.equals(key)) {
            return extern.getMetaData().getLocalHostname();
        }
        if (GuestExtern.GuestExternNames.MetaDataNames.HOSTNAME.equals(key)) {
            return extern.getMetaData().getHostname();
        }
        return "";
    }

    @Override
    public List<String> listMetaDataKeys(GuestEntity guest) {
        GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
        List<String> metaKeys = new ArrayList<>();
        if (extern != null && extern.getMetaData() != null) {
            GuestExtern.MetaData metaData = extern.getMetaData();
            if (!ObjectUtils.isEmpty(metaData.getHostname())) {
                metaKeys.add(GuestExtern.GuestExternNames.MetaDataNames.HOSTNAME );
            }
            if (!ObjectUtils.isEmpty(metaData.getLocalHostname())) {
                metaKeys.add(GuestExtern.GuestExternNames.MetaDataNames.LOCAL_HOSTNAME);
            }
            if (!ObjectUtils.isEmpty(metaData.getInstanceId())) {
                metaKeys.add(GuestExtern.GuestExternNames.MetaDataNames.INSTANCE_ID);
            }
        }
        return metaKeys;
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() != Constant.SystemCategory.WINDOWS;
    }
}
