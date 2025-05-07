package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.util.GuestExternNames;
import cn.chenjun.cloud.management.util.MetaDataType;
import com.google.gson.reflect.TypeToken;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class DefaultMetaDataService implements MetaDataService {


    @Override
    public MetaData buildCloudInitMetaData(GuestEntity guest) {
        Map<String, Map<String, String>> externMap = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
        Map<String, String> metaMap = externMap.getOrDefault(GuestExternNames.META_DATA, new HashMap<>(0));
        Set<String> metaNames = metaMap.entrySet().stream().map(t -> t.getKey() + ": " + t.getValue()).collect(Collectors.toSet());
        return MetaData.builder().type(MetaDataType.CLOUD).body(String.join("\r\n", metaNames)).build();
    }

    @Override
    public String findMetaDataByKey(GuestEntity guest, String key) {
        Map<String, Map<String, String>> externMap = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
        Map<String, String> metaMap = externMap.getOrDefault(GuestExternNames.META_DATA, new HashMap<>(0));
        return metaMap.getOrDefault(key, "");
    }

    @Override
    public List<String> listMetaDataKeys(GuestEntity guest) {

        Map<String, Map<String, String>> externMap = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
        Map<String, String> metaMap = externMap.getOrDefault(GuestExternNames.META_DATA, new HashMap<>(0));
        return new ArrayList<>(metaMap.keySet());
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() != SystemCategory.WINDOWS;
    }
}
