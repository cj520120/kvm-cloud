package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.JinjavaParser;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.data.mapper.TemplateMapper;
import cn.chenjun.cloud.management.data.mapper.VolumeMapper;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Service
public class TemplateVendorDataService implements VendorDataService {
    @Autowired
    protected ConfigService configService;
    @Autowired
    private VolumeMapper mapper;
    @Autowired
    private TemplateMapper templateMapper;

    @Override
    public MetaData load(GuestEntity guest) {
        StringBuilder sb = new StringBuilder();
        do {

            VolumeEntity volumeEntity = mapper.selectOne(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guest.getGuestId()).eq(VolumeEntity.DEVICE_ID, 0).last("limit 1"));
            if (volumeEntity == null) {
                break;
            }
            TemplateEntity template = templateMapper.selectById(volumeEntity.getTemplateId());
            if (template == null) {
                break;
            }
            String script = template.getScript();
            if (ObjectUtils.isEmpty(script)) {
                break;
            }
            sb.append(script);
        } while (false);
        String script = sb.toString();
        if (!ObjectUtils.isEmpty(script)) {
            List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(),
                    ConfigQuery.builder().type(Constant.ConfigType.HOST).id(guest.getHostId()).build(),
                    ConfigQuery.builder().type(Constant.ConfigType.GUEST).id(guest.getGuestId()).build()
            );
            Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
            Map<String, Object> map = new HashMap<>();
            map.put("__SYS__", sysconfig);
            map.put("vm", GsonBuilderUtil.create().fromJson(GsonBuilderUtil.create().toJson(guest), Map.class));
            script = JinjavaParser.create().render(script, map);
        }
        return MetaData.builder().type(MetaDataType.CLOUD).body(script).build();
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() != Constant.SystemCategory.WINDOWS;
    }

}
