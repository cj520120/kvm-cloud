package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.MetaDataEntity;
import cn.chenjun.cloud.management.data.mapper.MetaMapper;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class DefaultMetaDataService implements MetaDataService {
    @Autowired
    private MetaMapper metaMapper;



    @Override
    public MetaData buildCloudInitMetaData(GuestEntity guest) {
        List<MetaDataEntity> list = this.metaMapper.selectList(new QueryWrapper<MetaDataEntity>().eq(MetaDataEntity.GUEST_ID, guest.getGuestId()));
        Set<String> metaNames = list.stream().map(t -> t.getMetaKey() + ": " + t.getMetaValue()).collect(Collectors.toSet());
        return MetaData.builder().type(MetaDataType.CLOUD).body(String.join("\r\n", metaNames)).build();
    }

    @Override
    public String findMetaDataByKey(GuestEntity guest, String key) {
        MetaDataEntity entity = this.metaMapper.selectOne(new QueryWrapper<MetaDataEntity>().eq(MetaDataEntity.GUEST_ID, guest.getGuestId()).eq(MetaDataEntity.META_KEY, key));
        return entity == null ? "" : entity.getMetaValue();
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() != SystemCategory.WINDOWS;
    }
}
