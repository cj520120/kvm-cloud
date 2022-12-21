package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.bean.*;
import org.libvirt.Connect;

import java.util.List;

/**
 * @author chenjun
 */
public interface VolumeOperate {
    /**
     * 获取磁盘信息
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo getInfo(Connect connect, VolumeInfoRequest request) throws Exception;

    /**
     * 批量获取磁盘信息
     *
     * @param connect
     * @param batchRequest
     * @return
     * @throws Exception
     */
    List<VolumeInfo> batchInfo(Connect connect, List<VolumeInfoRequest> batchRequest) throws Exception;

    /**
     * 创建磁盘
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo create(Connect connect, VolumeCreateRequest request) throws Exception;

    /**
     * 删除磁盘
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroy(Connect connect, VolumeDestroyRequest request) throws Exception;

    /**
     * 克隆磁盘
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo clone(Connect connect, VolumeCloneRequest request) throws Exception;

    /**
     * 扩容磁盘
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo resize(Connect connect, VolumeResizeRequest request) throws Exception;

    /**
     * 创建磁盘快照
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo snapshot(Connect connect, VolumeCreateSnapshotRequest request) throws Exception;

    /**
     * 创建磁盘模版
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo template(Connect connect, VolumeCreateTemplateRequest request) throws Exception;

    /**
     * 下载磁盘
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo download(Connect connect, VolumeDownloadRequest request) throws Exception;

    /**
     * 迁移磁盘
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    VolumeInfo migrate(Connect connect, VolumeMigrateRequest request) throws Exception;
}
