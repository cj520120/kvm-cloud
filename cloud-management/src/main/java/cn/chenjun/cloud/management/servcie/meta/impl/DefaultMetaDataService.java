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
            GuestExtern.MetaDataExtern metaDataExtern = extern.getMetaData();
            if (!ObjectUtils.isEmpty(metaDataExtern.getHostname())) {
                metaDatas.add(GuestExtern.GuestExternNames.MetaDataNames.HOSTNAME + ": " + metaDataExtern.getHostname());
            }
            if (!ObjectUtils.isEmpty(metaDataExtern.getLocalHostname())) {
                metaDatas.add(GuestExtern.GuestExternNames.MetaDataNames.LOCAL_HOSTNAME + ": " + metaDataExtern.getLocalHostname());
            }
            if (!ObjectUtils.isEmpty(metaDataExtern.getInstanceId())) {
                metaDatas.add(GuestExtern.GuestExternNames.MetaDataNames.INSTANCE_ID + ": " + metaDataExtern.getInstanceId());
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
            GuestExtern.MetaDataExtern metaDataExtern = extern.getMetaData();
            if (!ObjectUtils.isEmpty(metaDataExtern.getHostname())) {
                metaKeys.add(GuestExtern.GuestExternNames.MetaDataNames.HOSTNAME );
            }
            if (!ObjectUtils.isEmpty(metaDataExtern.getLocalHostname())) {
                metaKeys.add(GuestExtern.GuestExternNames.MetaDataNames.LOCAL_HOSTNAME);
            }
            if (!ObjectUtils.isEmpty(metaDataExtern.getInstanceId())) {
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
