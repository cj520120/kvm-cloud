package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.management.data.entity.MetaDataEntity;
import cn.chenjun.cloud.management.data.mapper.MetaMapper;
import cn.chenjun.cloud.management.servcie.meta.MetaDataService;
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
    public boolean supports(@NonNull Integer systemCategory) {
        return true;
    }

    @Override
    public String loadMetaData(int guestId) {
        List<MetaDataEntity> list = this.metaMapper.selectList(new QueryWrapper<MetaDataEntity>().eq(MetaDataEntity.GUEST_ID, guestId));
        Set<String> metaNames = list.stream().map(t -> t.getMetaKey() + ": " + t.getMetaValue()).collect(Collectors.toSet());
        return String.join("\r\n", metaNames);
    }
}
