package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.operate.impl.VolumeOperateImpl;
import cn.chenjun.cloud.common.bean.VolumeCloneRequest;
import cn.chenjun.cloud.common.bean.VolumeCreateRequest;
import cn.chenjun.cloud.common.bean.VolumeDestroyRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VolumeOperateImplTest extends AbstractTest {

    public static final String STORAGE_NAME = "94053493-10c2-3403-8c0b-40da73eee0e6";
    public static final String VOLUME_NAME = "TEST_VOLUME";
    public static final String VOLUME_TYPE = "qcow2";
    public static final String CLONE_VOLUME_NAME = "TEST_CLONE_VOLUME";
    public static final String CLONE_VOLUME_TYPE = "qcow2";
    public static final long VOLUME_SIZE = 1024 * 1024 * 100L;

    @InjectMocks
    private VolumeOperateImpl volumeOperate;


    @Test
    @SneakyThrows
    public void test_001_create() {
        VolumeCreateRequest request = VolumeCreateRequest.builder()
                .targetName(VOLUME_NAME)
                .targetSize(VOLUME_SIZE)
                .targetStorage(STORAGE_NAME)
                .targetType(VOLUME_TYPE)
                .build();
        log.info("create volume={}", volumeOperate.create(connect, request));
    }

//    @Test
//    @SneakyThrows
//    public void test_002_resize() {
//        VolumeResizeRequest request = VolumeResizeRequest.builder()
//                .sourceName(VOLUME_NAME)
//                .sourceStorage(STORAGE_NAME)
//                .size(VOLUME_SIZE * 2)
//                .build();
//        log.info("resize volume={}", volumeOperate.resize(connect, request));
//    }


    @Test
    @SneakyThrows
    public void test_003_clone() {
        VolumeCloneRequest request = VolumeCloneRequest.builder()
                .targetName(CLONE_VOLUME_NAME)
                .targetStorage(STORAGE_NAME)
                .sourceName(VOLUME_NAME)
                .sourceStorage(STORAGE_NAME)
                .targetType(CLONE_VOLUME_TYPE)
                .size(VOLUME_SIZE * 2)
                .build();
        log.info("clone volume={}", volumeOperate.clone(connect, request));
    }

//    @Test
//    @SneakyThrows
//    public void test_004_getInfo() {
//
//        VolumeInfoRequest request = VolumeInfoRequest.builder()
//                .sourceName(VOLUME_NAME)
//                .sourceStorage(STORAGE_NAME)
//                .build();
//        log.info("info volume={}", volumeOperate.getInfo(connect, request));
//    }
//
//    @Test
//    @SneakyThrows
//    public void test_005_batchInfo() {
//        VolumeInfoRequest request = VolumeInfoRequest.builder()
//                .sourceName(VOLUME_NAME)
//                .sourceStorage(STORAGE_NAME)
//                .build();
//        List<VolumeInfo> volumeInfoList = volumeOperate.batchInfo(connect, Collections.singletonList(request));
//        Assert.assertNotNull(volumeInfoList.get(0));
//    }

    @Test
    @SneakyThrows
    public void test_006_destroy() {
        VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                .sourceName(VOLUME_NAME)
                .sourceStorage(STORAGE_NAME)
                .build();
        volumeOperate.destroy(connect, request);

        request = VolumeDestroyRequest.builder()
                .sourceName(CLONE_VOLUME_NAME)
                .sourceStorage(STORAGE_NAME)
                .build();
        volumeOperate.destroy(connect, request);
    }
}