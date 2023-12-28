package cn.chenjun.cloud.test;

import cn.chenjun.cloud.agent.operate.impl.NetworkOperateImpl;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.util.Constant;
import lombok.SneakyThrows;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NetworkOperateImplTest extends AbstractTest {

    public static final String BASIC_NETWORK_NAME = "E397CD4E-8E12-4F40-A23E-85434572ABA5";
    public static final String VLAN_NETWORK_NAME = "43320B61-ACA0-41D7-99E1-4374C42B4FDD";

    @InjectMocks
    private NetworkOperateImpl networkOperate;


    @Test
    @SneakyThrows
    public void test_01_createBasic() {
        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder().poolId(BASIC_NETWORK_NAME)
                .bridgeType(Constant.NetworkBridgeType.OPEN_SWITCH)
                .bridge("br0")
                .build();
        networkOperate.createBasic(this.connect, basicBridgeNetwork);
    }

    @Test
    @SneakyThrows
    public void test_02_createVlan() {
        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder().poolId(BASIC_NETWORK_NAME)
                .bridgeType(Constant.NetworkBridgeType.OPEN_SWITCH)
                .bridge("br0")
                .build();
        VlanNetwork vlanNetwork = VlanNetwork.builder().poolId(VLAN_NETWORK_NAME)
                .basic(basicBridgeNetwork)
                .vlanId(100)
                .bridge("br0")
                .build();
        networkOperate.createVlan(this.connect, vlanNetwork);
    }

    @Test
    @SneakyThrows
    public void test_03_destroyBasic() {
        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder().poolId(BASIC_NETWORK_NAME)
                .bridgeType(Constant.NetworkBridgeType.OPEN_SWITCH)
                .bridge("br0")
                .build();
        networkOperate.destroyBasic(this.connect, basicBridgeNetwork);
    }

    @Test
    @SneakyThrows
    public void test_04_destroyVlan() {
        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder().poolId(BASIC_NETWORK_NAME)
                .bridgeType(Constant.NetworkBridgeType.OPEN_SWITCH)
                .bridge("br0")
                .build();
        VlanNetwork vlanNetwork = VlanNetwork.builder().poolId(VLAN_NETWORK_NAME)
                .basic(basicBridgeNetwork)
                .vlanId(100)
                .bridge("br0")
                .build();
        networkOperate.destroyVlan(this.connect, vlanNetwork);
    }
}