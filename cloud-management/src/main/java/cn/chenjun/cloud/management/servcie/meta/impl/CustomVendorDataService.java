package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.GuestExtern;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomVendorDataService implements VendorDataService {
    @Override
    public MetaData load(GuestEntity guest) {
        if (ObjectUtils.isEmpty(guest) || ObjectUtils.isEmpty(guest.getExtern())) {
            return null;
        }
        GuestExtern extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), GuestExtern.class);
        return MetaData.builder().body(extern.getInitVendorData()).type(MetaDataType.CLOUD).build();
    }

    @Override
    public boolean supports(GuestEntity entity) {
        return true;
    }
}
