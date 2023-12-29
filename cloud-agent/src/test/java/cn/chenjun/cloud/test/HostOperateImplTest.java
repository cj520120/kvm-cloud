package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.operate.StorageOperate;
import cn.chenjun.cloud.agent.operate.impl.HostOperateImpl;
import cn.chenjun.cloud.common.bean.HostInfo;
import cn.chenjun.cloud.common.bean.InitHostRequest;
import cn.chenjun.cloud.common.bean.NoneRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HostOperateImplTest extends AbstractTest {
    @InjectMocks
    private HostOperateImpl hostOperate;

    @Mock
    private NetworkOperate networkOperate;
    @Mock
    private StorageOperate storageOperate;


    @SneakyThrows
    public void init() {
        Mockito.doNothing().when(networkOperate).createBasic(any(), any());
        Mockito.doNothing().when(networkOperate).destroyBasic(any(), any());
        Mockito.doNothing().when(networkOperate).createVlan(any(), any());
        Mockito.doNothing().when(networkOperate).destroyVlan(any(), any());
        Mockito.doNothing().when(storageOperate).destroy(any(), any());
        Mockito.when(storageOperate.create(any(), any())).thenReturn(null);
        Mockito.when(storageOperate.getStorageInfo(any(), any())).thenReturn(StorageInfo.builder().build());
        Mockito.when(storageOperate.batchStorageInfo(any(), any())).thenReturn(new ArrayList<>(0));

    }

    @Test
    @SneakyThrows
    public void test_01_getHostInfo() {
        HostInfo hostInfo = this.hostOperate.getHostInfo(connect, NoneRequest.builder().build());
        Assert.assertNotNull(hostInfo);
    }
    @Test
    @SneakyThrows
    public void test_02_initHost(){
        InitHostRequest request= InitHostRequest.builder().basicBridgeNetworkList(new ArrayList<>(0))
                .vlanNetworkList(new ArrayList<>())
                .storageList(new ArrayList<>())
                .build();
        HostInfo hostInfo =  this.hostOperate.initHost(connect,request);
        log.info("host={}",hostInfo);
    }

}