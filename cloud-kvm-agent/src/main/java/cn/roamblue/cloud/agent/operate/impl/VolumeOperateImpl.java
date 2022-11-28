package cn.roamblue.cloud.agent.operate.impl;

import cn.roamblue.cloud.agent.operate.VolumeOperate;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.agent.VolumeRequest;
import org.libvirt.Connect;

public class VolumeOperateImpl implements VolumeOperate {
    @Override
    public VolumeModel create(Connect connect, VolumeRequest.CreateVolume request) throws Exception {
        /**
         * 1、父文件格式只能为qcow2格式
         * 2、如果文件不为qcow格式，则在存在父文件的情况下直接clone过来，否则利用backupfile
         */
        return null;
    }

    @Override
    public void destroy(Connect connect, VolumeRequest.DestroyVolume request) throws Exception {

    }

    @Override
    public VolumeModel clone(Connect connect, VolumeRequest.CloneVolume request) throws Exception {
        return null;
    }

    @Override
    public VolumeModel resize(Connect connect, VolumeRequest.ResizeVolume request) throws Exception {
        return null;
    }

    @Override
    public VolumeModel snapshot(Connect connect, VolumeRequest.SnapshotVolume request) throws Exception {
        return null;
    }

    @Override
    public VolumeModel template(Connect connect, VolumeRequest.TemplateVolume request) throws Exception {
        return null;
    }

    @Override
    public VolumeModel download(Connect connect, VolumeRequest.DownloadVolume request) throws Exception {
        return null;
    }

    @Override
    public VolumeModel migrate(Connect connect, VolumeRequest.MigrateVolume request) throws Exception {
        return null;
    }
}
