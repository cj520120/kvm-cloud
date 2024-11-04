package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.management.data.entity.GuestDiskEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.data.mapper.GuestDiskMapper;
import cn.chenjun.cloud.management.data.mapper.TemplateMapper;
import cn.chenjun.cloud.management.data.mapper.VolumeMapper;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author chenjun
 */
@Service
public class DefaultVendorDataService implements VendorDataService {
    @Autowired
    private VolumeMapper mapper;
    @Autowired
    private GuestDiskMapper diskMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Override
    public String loadVendorData(int guestId) {
        StringBuilder sb = new StringBuilder();
        do {
            GuestDiskEntity guestDisk = diskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.GUEST_ID, guestId).eq(GuestDiskEntity.DEVICE_ID, 0).last("limit 1"));
            if (guestDisk == null) {
                break;
            }
            VolumeEntity volumeEntity = mapper.selectById(guestDisk.getVolumeId());
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
        return sb.toString();
    }


}
