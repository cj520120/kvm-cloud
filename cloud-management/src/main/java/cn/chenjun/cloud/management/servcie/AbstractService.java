package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.dao.*;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.util.ConfigKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractService {
    @Autowired
    protected GuestDao guestDao;
    @Autowired
    protected StorageDao storageDao;
    @Autowired
    protected GuestNetworkDao guestNetworkDao;
    @Autowired
    protected HostDao hostDao;
    @Autowired
    protected VolumeDao volumeDao;
    @Autowired
    protected NetworkDao networkDao;
    @Autowired
    protected TemplateDao templateDao;
    @Autowired
    protected TemplateVolumeDao templateVolumeDao;
    @Autowired
    @Lazy
    protected TaskService operateTask;


    @Autowired
    protected SchemeDao schemeDao;
    @Autowired
    protected ComponentDao componentDao;
    @Autowired
    protected SshAuthorizedDao sshAuthorizedDao;

    @Autowired
    protected ConfigService configService;
    @Autowired
    protected GroupDao groupDao;


    public GuestEntity getGuestById(int guestId) {
        GuestEntity entity = this.guestDao.findById(guestId);
        if (entity == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机不存在");
        }
        return entity;
    }

    public HostEntity getHostById(int hostId) {
        HostEntity entity = this.hostDao.findById(hostId);
        if (entity == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        return entity;
    }

    public StorageEntity getStorageById(int storageId) {
        StorageEntity entity = this.storageDao.findById(storageId);
        if (entity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        return entity;
    }

    public NetworkEntity getNetworkById(int networkId) {
        NetworkEntity entity = this.networkDao.findById(networkId);
        if (entity == null) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        return entity;
    }

    public VolumeEntity getVolumeById(int volumeId) {
        VolumeEntity entity = this.volumeDao.findById(volumeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        return entity;
    }
    protected String getVolumeType(StorageEntity storage) {
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        return volumeType;
    }

    public GuestEntity getVolumeGuest(int volumeId) {
        VolumeEntity volume = this.getVolumeById(volumeId);
        return this.getGuestById(volume.getGuestId());
    }

}
