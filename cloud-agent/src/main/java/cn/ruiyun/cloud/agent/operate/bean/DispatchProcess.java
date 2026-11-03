package cn.ruiyun.cloud.agent.operate.bean;

import cn.ruiyun.cloud.common.bean.TaskRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchProcess {
    private TaskRequest task;

    private Dispatch<?, ?> dispatch;
}
