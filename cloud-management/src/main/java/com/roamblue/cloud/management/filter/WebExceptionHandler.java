package com.roamblue.cloud.management.filter;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
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
    public ResultUtil<Exception> exceptionHandler(Exception error) {
        if (error instanceof CodeException) {
            return ResultUtil.<Exception>builder().code(((CodeException) error).getCode()).message(error.getMessage()).build();
        } else {
            log.error("", error);
            return ResultUtil.<Exception>builder().data(error).code(500).message(error.getMessage()).build();
        }
    }
}
