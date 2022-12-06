package cn.roamblue.cloud.management.operate.impl;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.TaskRequest;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.operate.Operate;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.task.LocalReportTask;
import cn.roamblue.cloud.management.task.OperateTask;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;

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
    protected GuestVncMapper guestVncMapper;
    @Autowired
    protected LocalReportTask localReportTask;

    @Autowired
    @Lazy
    protected OperateTask operateTask;

    protected AbstractOperate(Class<T> paramType) {
        this.paramType = paramType;
    }

    @Override
    public Class<T> getParamType() {
        return this.paramType;
    }

    protected V syncInvoker(HostEntity host, T param, String command, Object data) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("data", GsonBuilderUtil.create().toJson(data));
        map.put("command", command);
        String uri = String.format("%s/api/operate", host.getUri());
        String response = HttpUtil.post(uri, map);
        return GsonBuilderUtil.create().fromJson(response, this.getCallResultType());
    }

    protected void asyncInvoker(HostEntity host, T param, String command, Object data) {
        TaskRequest taskRequest = TaskRequest.builder()
                .command(command)
                .data(GsonBuilderUtil.create().toJson(data))
                .taskId(param.getTaskId()).build();
        Map<String, Object> map = new HashMap<>(2);
        map.put("data", GsonBuilderUtil.create().toJson(taskRequest));
        map.put("command", Constant.Command.SUBMIT_TASK);
        String uri = String.format("%s/api/operate", host.getUri());
        String response = HttpUtil.post(uri, map);
        GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<Void>>() {
        }.getType());
    }

    @Override
    public void onFinish(T param, V resultUtil) {

    }

    protected void onSubmitFinishEvent(String taskId, V result) {
        this.operateTask.onTaskFinish(taskId, GsonBuilderUtil.create().toJson(result));
    }
}
