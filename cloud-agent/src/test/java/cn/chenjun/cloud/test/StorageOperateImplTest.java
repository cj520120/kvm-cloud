package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.operate.impl.StorageOperateImpl;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageDestroyRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.bean.StorageInfoRequest;
import cn.chenjun.cloud.common.util.Constant;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorageOperateImplTest extends AbstractTest {

    public static final String STORAGE_NAME = "E397CD4E-8E12-4F40-A23E-85434572ABA5";

    @InjectMocks
    private StorageOperateImpl storageOperate;


    @Test
    @SneakyThrows
    public void test_01_create() {
        Map<String, Object> param = new HashMap<>(2);
        param.put("uri", "192.168.1.119:24007,192.168.1.131:24007,192.168.1.132:24007,192.168.1.134:24007");
        param.put("path", "libvirt");
        StorageCreateRequest request = StorageCreateRequest.builder()
                .name(STORAGE_NAME)
                .type(Constant.StorageType.GLUSTERFS)
                .param(param).build();
        storageOperate.create(this.connect, request);
    }

    @Test
    @SneakyThrows
    public void test_02_getStorageInfo() {
        storageOperate.getStorageInfo(connect, StorageInfoRequest.builder().name(STORAGE_NAME).build());
    }

    @Test
    @SneakyThrows
    public void test_03_batchStorageInfo() {
        List<StorageInfo> list = storageOperate.batchStorageInfo(connect, Collections.singletonList(StorageInfoRequest.builder().name(STORAGE_NAME).build()));
        Assert.assertNotNull(list.get(0));
    }

    @Test
    @SneakyThrows
    public void test_04_destroy() {
        storageOperate.destroy(connect, StorageDestroyRequest.builder().name(STORAGE_NAME).build());
    }
}