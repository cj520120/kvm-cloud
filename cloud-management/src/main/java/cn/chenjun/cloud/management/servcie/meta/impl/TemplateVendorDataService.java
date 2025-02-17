package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.data.entity.GuestDiskEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import cn.chenjun.cloud.management.util.MetaDataType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author chenjun
 */
@Service
public class TemplateVendorDataService implements VendorDataService {
    @Autowired
    protected GuestSshMapper guestSshMapper;
    @Autowired
    protected SshAuthorizedMapper sshAuthorizedMapper;
    @Autowired
    private GuestPasswordMapper guestPasswordMapper;
    @Autowired
    private VolumeMapper mapper;
    @Autowired
    private GuestDiskMapper diskMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Override
    public MetaData load(GuestEntity guest) {
        StringBuilder sb = new StringBuilder();
        do {
            GuestDiskEntity guestDisk = diskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.GUEST_ID, guest.getGuestId()).eq(GuestDiskEntity.DEVICE_ID, 0).last("limit 1"));
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
        return MetaData.builder().type(MetaDataType.CLOUD).body(sb.toString()).build();
    }

    @Override
    public boolean supports(@NonNull GuestEntity guest) {
        return guest.getSystemCategory() != SystemCategory.WINDOWS;
    }

}
