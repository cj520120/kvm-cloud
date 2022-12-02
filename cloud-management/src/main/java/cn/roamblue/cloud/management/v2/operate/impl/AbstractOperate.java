package cn.roamblue.cloud.management.v2.operate.impl;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.TaskRequest;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.operate.Operate;
import cn.roamblue.cloud.management.v2.operate.bean.BaseOperateInfo;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenjun
 */
public abstract class AbstractOperate<T extends BaseOperateInfo, V extends ResultUtil> implements Operate<T, V> {
    private final Class<T> paramType;

    protected AbstractOperate(Class<T> paramType) {
        this.paramType = paramType;
    }

    @Override
    public Class<T> getParamType() {
        return this.paramType;
    }

    protected V call(HostEntity host, T param, String command, Object data) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("data", GsonBuilderUtil.create().toJson(data));
        map.put("command",command);
        String uri = String.format("http://%s:%d/api/operate", host.getIp(), host.getPort());
        String response = HttpUtil.post(uri, map);
        return GsonBuilderUtil.create().fromJson(response, this.getCallResultType());
    }

    protected void asyncCall(HostEntity host, T param, String command, Object data) {
        TaskRequest taskRequest = TaskRequest.builder()
                .command(command)
                .data(GsonBuilderUtil.create().toJson(data))
                .taskId(param.getTaskId()).build();
        Map<String, Object> map = new HashMap<>(2);
        map.put("data", GsonBuilderUtil.create().toJson(taskRequest));
        map.put("command", Constant.Command.SUBMIT_TASK);
        String uri = String.format("http://%s:%d/api/operate", host.getIp(), host.getPort());
        String response = HttpUtil.post(uri, map);
        GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<Void>>() {
        }.getType());
    }

}
