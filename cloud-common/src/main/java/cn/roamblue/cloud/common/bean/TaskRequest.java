package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础任务
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    private String taskId;
    private String command;
    private String data;

}
