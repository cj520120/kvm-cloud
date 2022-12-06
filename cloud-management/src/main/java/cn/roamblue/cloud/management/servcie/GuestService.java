package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.model.GuestModel;
import cn.roamblue.cloud.management.util.Constant;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
public class GuestService {

    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private GuestDiskMapper guestDiskMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private TemplateMapper templateMapper;

    public StorageEntity allocateStorage(int storageId) {
        StorageEntity storage;
        if (storageId > 0) {
            storage = storageMapper.selectById(storageId);
            if (storage == null) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
            }
        } else {
            List<StorageEntity> storageList = storageMapper.selectList(new QueryWrapper<>());
            storageList = storageList.stream().filter(t -> Objects.equals(t.getStatus(), Constant.StorageStatus.READY)).collect(Collectors.toList());
            storage = storageList.stream().sorted((o1, o2) -> Long.compare(o2.getAvailable(), o1.getAvailable())).findFirst().orElse(null);
        }
        return storage;
    }



    private GuestModel initGuestInfo(GuestEntity entity) {
        return GuestModel.builder().build();
    }

    public ResultUtil<List<GuestModel>> listGuests() {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> getGuestInfo(int guestId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> createGuest(String description,String busType,int cpu,long memory,int networkId,int cdRoom,int templateId,
                                              int storageId,int volumeType,long size) {


        String guestName= UUID.randomUUID().toString().replace("-","");
        GuestEntity guest=GuestEntity.builder()
                .name(guestName)
                .description(description)
                .busType(busType)
                .cpu(cpu)
                .memory(memory)
                .cdRoom(cdRoom)
                .hostId(0)
                .lastHostId(0)
                .type(Constant.GuestType.USER)
                .status(Constant.GuestStatus.CREATING)
                .build();


        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> modifyGuest(int guestId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> attachCdRoom(int guestId, int templateId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> detachCdRoom(int guestId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> attachDisk(int guestId, int volumeId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> detachDisk(int guestId, int guestDiskId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> attachNetwork(int guestId, int networkId) {
        return ResultUtil.success(null);
    }

    public ResultUtil<GuestModel> detachNetwork(int guestId, int guestNetworkId) {
        return ResultUtil.success(null);
    }
}
