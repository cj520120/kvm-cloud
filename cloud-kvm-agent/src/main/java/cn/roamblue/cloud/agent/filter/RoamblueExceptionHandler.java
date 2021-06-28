package cn.roamblue.cloud.agent.filter;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author chenjun
 */

/**
 * @author chenjun
 */
@ControllerAdvice
public class RoamblueExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultUtil<Exception> exceptionHandler(Exception error) {
        if (error instanceof CodeException) {
            return ResultUtil.<Exception>builder().code(((CodeException) error).getCode()).message(error.getMessage()).build();
        } else {
            return ResultUtil.<Exception>builder().data(error).code(ErrorCode.SERVER_ERROR).message(error.getMessage()).build();
        }
    }
}
