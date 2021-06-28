package cn.roamblue.cloud.common.bean;

import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回结果
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultUtil<T> {
    /**
     * 应答码
     */
    @Builder.Default
    private int code = ErrorCode.SUCCESS;
    /**
     * 消息
     */
    private String message;
    /**
     * 应答数据
     */
    private T data;

    public static <T> ResultUtil<T> success(T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(ErrorCode.SUCCESS);
        result.setData(data);
        return result;
    }

    public static <T> ResultUtil<T> error(int code, String message) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
