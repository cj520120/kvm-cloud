package com.roamblue.cloud.common.bean;

import com.roamblue.cloud.common.util.ErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("返回结果")
public class ResultUtil<T> {
    @ApiModelProperty("应答码")
    private int code;
    @ApiModelProperty("应答码")
    private String message;
    @ApiModelProperty("应答数据")
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
