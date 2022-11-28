package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.agent.VolumeRequest;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public interface VolumeOperate {
    /**
     * 创建磁盘
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel create(Connect connect, VolumeRequest.CreateVolume request) throws Exception;

    /**
     * 删除磁盘
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroy(Connect connect,VolumeRequest.DestroyVolume request) throws Exception;
    /**
     * 克隆磁盘
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel clone(Connect connect,VolumeRequest.CloneVolume request) throws Exception;
    /**
     * 扩容磁盘
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel resize(Connect connect,VolumeRequest.ResizeVolume request) throws Exception;
    /**
     * 创建磁盘快照
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel snapshot(Connect connect,VolumeRequest.SnapshotVolume request) throws Exception;
    /**
     * 创建磁盘模版
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel template(Connect connect,VolumeRequest.TemplateVolume request) throws Exception;
    /**
     * 下载磁盘
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel download(Connect connect,VolumeRequest.DownloadVolume request) throws Exception;
    /**
     * 迁移磁盘
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeModel migrate(Connect connect,VolumeRequest.MigrateVolume request) throws Exception;
}
