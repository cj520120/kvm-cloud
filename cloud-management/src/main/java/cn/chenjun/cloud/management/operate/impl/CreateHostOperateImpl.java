package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.CreateHostOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateHostOperateImpl extends AbstractOperate<CreateHostOperate, ResultUtil<HostInfo>> {


    @Override
    public void operate(CreateHostOperate param) {
        HostEntity host = this.hostMapper.selectById(param.getHostId());
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(cn.chenjun.cloud.management.util.Constant.StorageStatus.READY, t.getStatus())).collect(Collectors.toList());
        List<NetworkEntity> networkList = this.networkMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(cn.chenjun.cloud.management.util.Constant.NetworkStatus.READY, t.getStatus())).collect(Collectors.toList());
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.DEFAULT).id(0).build());
        queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.HOST).id(host.getHostId()).build());
        Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
        List<StorageCreateRequest> createStorageRequest = storageList.stream().map(storage -> buildStorageCreateRequest(storage, sysconfig)).collect(Collectors.toList());
        Map<Integer, NetworkEntity> basicBridgeNetworkMap = networkList.stream().collect(Collectors.toMap(NetworkEntity::getNetworkId, Function.identity()));
        List<BasicBridgeNetwork> basicBridgeNetworks = new ArrayList<>();
        List<VlanNetwork> vlanNetworkList = new ArrayList<>();
        for (NetworkEntity network : networkList) {
            if (Objects.equals(cn.chenjun.cloud.management.util.Constant.NetworkType.VLAN, network.getType())) {
                NetworkEntity basicBridgeNetwork = basicBridgeNetworkMap.get(network.getBasicNetworkId());
                if (basicBridgeNetwork != null) {
                    vlanNetworkList.add(this.buildVlanCreateRequest(basicBridgeNetwork, network, sysconfig));
                }
            } else {
                basicBridgeNetworks.add(this.buildBasicNetworkRequest(network, sysconfig));
            }
        }
        String uri = String.format("%s/api/init", host.getUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("managerUri", (String) sysconfig.get(cn.chenjun.cloud.management.util.Constant.ConfigKey.DEFAULT_CLUSTER_MANAGER_URI));
        requestMap.add("clientId", host.getClientId());
        requestMap.add("clientSecret", host.getClientSecret());
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(URI.create(uri))
                .headers(httpHeaders)
                .body(requestMap);
        ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "初始化主机出错.status=" + responseEntity.getStatusCode());
        }
        ResultUtil<Void> resultUtil = GsonBuilderUtil.create().fromJson(responseEntity.getBody(), new TypeToken<ResultUtil<Void>>() {
        }.getType());
        if (Objects.requireNonNull(resultUtil).getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }

        InitHostRequest request = InitHostRequest.builder()
                .storageList(createStorageRequest)
                .basicBridgeNetworkList(basicBridgeNetworks)
                .vlanNetworkList(vlanNetworkList)
                .build();
        this.asyncInvoker(host, param, Constant.Command.HOST_INIT, request);
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<HostInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(CreateHostOperate param, ResultUtil<HostInfo> resultUtil) {
        HostEntity host = HostEntity.builder().hostId(param.getHostId()).build();
        if (host != null) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                HostInfo hostInfo = resultUtil.getData();
                host.setThreads(hostInfo.getThreads());
                host.setCores(hostInfo.getCores());
                host.setSockets(hostInfo.getSockets());
                host.setArch(hostInfo.getArch());
                host.setHypervisor(hostInfo.getHypervisor());
                host.setTotalCpu(hostInfo.getCpu());
                host.setTotalMemory(hostInfo.getMemory());
                host.setEmulator(hostInfo.getEmulator());
                host.setHostName(hostInfo.getHostName());

                host.setStatus(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE);
            } else {
                host.setStatus(cn.chenjun.cloud.management.util.Constant.HostStatus.ERROR);
            }
            this.hostMapper.updateById(host);
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getHostId()).type(Constant.NotifyType.UPDATE_HOST).build());

    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CREATE_HOST;
    }
}
