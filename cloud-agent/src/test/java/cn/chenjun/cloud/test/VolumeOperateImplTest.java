package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.operate.impl.VolumeOperateImpl;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.util.Constant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VolumeOperateImplTest extends AbstractTest {

    public static final String STORAGE_NAME = "E397CD4E-8E12-4F40-A23E-85434572ABA5";
    public static final String VOLUME_NAME = "TEST_VOLUME";
    public static final String VOLUME_TYPE = "qcow2";
    public static final String CLONE_VOLUME_NAME = "TEST_CLONE_VOLUME";
    public static final String CLONE_VOLUME_TYPE = "qcow2";
    public static final long VOLUME_SIZE = 1024 * 1024 * 100L;

    @InjectMocks
    private VolumeOperateImpl volumeOperate;

    private Storage initGlusterStorage(String name) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("uri", "192.168.1.119,192.168.1.131,192.168.1.132,192.168.1.134:24007");
        param.put("path", "libvirt");
        return Storage.builder().type(Constant.StorageType.GLUSTERFS).name(name).param(param).build();
    }

    private Volume initGlusterVolume(String storage, String name, String type, long size) {
        return Volume.builder().storage(initGlusterStorage(storage)).name(name).capacity(size).type(type).build();
    }

    @Test
    @SneakyThrows
    public void test_001_create() {
        VolumeCreateRequest request = VolumeCreateRequest.builder()
                .volume(initGlusterVolume(STORAGE_NAME, VOLUME_NAME, VOLUME_TYPE, VOLUME_SIZE))
                .build();
        log.info("create volume={}", volumeOperate.create(connect, request));
    }

    @Test
    @SneakyThrows
    public void test_002_resize() {
        VolumeResizeRequest request = VolumeResizeRequest.builder()
                .volume(initGlusterVolume(STORAGE_NAME, VOLUME_NAME, VOLUME_TYPE, VOLUME_SIZE))
                .size(VOLUME_SIZE * 2)
                .build();
        log.info("resize volume={}", volumeOperate.resize(connect, request));
    }


    @Test
    @SneakyThrows
    public void test_003_clone() {
        VolumeCloneRequest request = VolumeCloneRequest.builder()
                .sourceVolume(initGlusterVolume(STORAGE_NAME, VOLUME_NAME, VOLUME_TYPE, VOLUME_SIZE))
                .targetVolume(initGlusterVolume(STORAGE_NAME, CLONE_VOLUME_NAME, CLONE_VOLUME_TYPE, VOLUME_SIZE * 2))
                .build();
        log.info("clone volume={}", volumeOperate.clone(connect, request));
    }

    @Test
    @SneakyThrows
    public void test_004_getInfo() {

        VolumeInfoRequest request = VolumeInfoRequest.builder()
                .sourceName(VOLUME_NAME)
                .sourceStorage(STORAGE_NAME)
                .build();
        log.info("info volume={}", volumeOperate.getInfo(connect, request));
    }

    @Test
    @SneakyThrows
    public void test_005_batchInfo() {
        VolumeInfoRequest request = VolumeInfoRequest.builder()
                .sourceName(VOLUME_NAME)
                .sourceStorage(STORAGE_NAME)
                .build();
        List<VolumeInfo> volumeInfoList = volumeOperate.batchInfo(connect, Collections.singletonList(request));
        Assert.assertNotNull(volumeInfoList.get(0));
    }

    @Test
    @SneakyThrows
    public void test_006_destroy() {
        VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                .volume(initGlusterVolume(STORAGE_NAME, VOLUME_NAME, VOLUME_TYPE, VOLUME_SIZE))
                .build();
        volumeOperate.destroy(connect, request);

        request = VolumeDestroyRequest.builder()
                .volume(initGlusterVolume(STORAGE_NAME, CLONE_VOLUME_NAME, CLONE_VOLUME_TYPE, VOLUME_SIZE))
                .build();
        volumeOperate.destroy(connect, request);
    }
}