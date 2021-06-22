package com.roamblue.cloud.management.filter;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author chenjun
 */

/**
 * @author chenjun
 */
@Slf4j
@ControllerAdvice
public class WebExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultUtil exceptionHandler(Exception error) {
        if (error instanceof CodeException) {
            CodeException codeException = (CodeException) error;
            return ResultUtil.error(codeException.getCode(), codeException.getMessage());
        } else {
            log.error("request fail.", error);
            return ResultUtil.error(ErrorCode.SERVER_ERROR, error.getMessage());
        }
    }
}
