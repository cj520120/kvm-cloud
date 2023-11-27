package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.TaskRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.operate.Operate;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.servcie.AllocateService;
import cn.chenjun.cloud.management.websocket.cluster.ClusterService;
import cn.chenjun.cloud.management.task.OperateTask;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author chenjun
 */
public abstract class AbstractOperate<T extends BaseOperateParam, V extends ResultUtil> implements Operate<T, V> {
    private final Class<T> paramType;
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    protected GuestDiskMapper guestDiskMapper;
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected HostMapper hostMapper;
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected StorageMapper storageMapper;
    @Autowired
    protected TemplateMapper templateMapper;
    @Autowired
    protected TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    protected VolumeMapper volumeMapper;

    @Autowired
    protected SchemeMapper schemeMapper;
    @Autowired
    protected SnapshotVolumeMapper snapshotVolumeMapper;
    @Autowired
    protected AllocateService allocateService;
    @Autowired
    protected ClusterService clusterService;
    @Autowired
    @Lazy
    protected OperateTask operateTask;
    @Autowired
    protected ApplicationConfig applicationConfig;
    @Autowired
    protected RestTemplate restTemplate;

    protected AbstractOperate(Class<T> paramType) {
        this.paramType = paramType;
    }

    @Override
    public Class<T> getParamType() {
        return this.paramType;
    }


    protected void asyncInvoker(HostEntity host, T param, String command, Object data) {
        TaskRequest taskRequest = TaskRequest.builder()
                .command(command)
                .data(GsonBuilderUtil.create().toJson(data))
                .taskId(param.getTaskId()).build();
        String nonce = String.valueOf(System.nanoTime());
        Map<String, Object> map = new HashMap<>(6);
        map.put("data", GsonBuilderUtil.create().toJson(taskRequest));
        map.put("taskId", UUID.randomUUID().toString());
        map.put("command", Constant.Command.SUBMIT_TASK);
        map.put("timestamp", System.currentTimeMillis());
        try {
            String sign = AppUtils.sign(map, host.getClientId(), host.getClientSecret(), nonce);
            map.put("sign", sign);
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "数据签名错误");
        }
        String uri = String.format("%s/api/operate", host.getUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        map.forEach((k, v) -> requestMap.add(k, v.toString()));
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(URI.create(uri))
                .headers(httpHeaders)
                .body(requestMap);
        ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "请求出错.status=" + responseEntity.getStatusCode());
        }
        String response = responseEntity.getBody();
        ResultUtil<Void> resultUtil = GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<Void>>() {
        }.getType());
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }
    }

    @Override
    public void onFinish(T param, V resultUtil) {

    }

    protected void onSubmitFinishEvent(String taskId, V result) {
        this.operateTask.onTaskFinish(taskId, GsonBuilderUtil.create().toJson(result));
    }
}
