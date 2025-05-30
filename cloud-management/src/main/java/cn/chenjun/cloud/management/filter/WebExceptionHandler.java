package cn.chenjun.cloud.management.filter;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.ValidationException;
import java.text.MessageFormat;

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
    public ResultUtil<Void> codeExceptionHandler(HttpServletRequest request, Exception error) {

        if (error instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException exception = ((MissingServletRequestParameterException) error);
            String msg = MessageFormat.format("process controller param error.uri:{0},param:{1},method={2},msg={3}", request.getRequestURI(), exception.getParameterName(), request.getMethod(), error.getMessage());
            log.warn(msg);
            return ResultUtil.<Void>builder().code(ErrorCode.PARAM_ERROR).message(msg).build();
        } else if (error instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException exception = ((MethodArgumentTypeMismatchException) error);
            String msg = MessageFormat.format("process controller parse param error.uri:{0},param:[{1}={2}] type=[{3}],method={4}", request.getRequestURI(), exception.getName(), exception.getValue(), exception.getRequiredType().getName(), request.getMethod());
            log.warn(msg);
            return ResultUtil.<Void>builder().code(ErrorCode.PARAM_ERROR).message(msg).build();
        } else if (error instanceof CodeException) {
            return ResultUtil.<Void>builder().code(((CodeException) error).getCode()).build();
        } else if (error instanceof HttpRequestMethodNotSupportedException) {
            HttpRequestMethodNotSupportedException exception = (HttpRequestMethodNotSupportedException) error;
            String msg = MessageFormat.format("process controller not support method.uri:{0},support={1},request={2}", request.getRequestURI(), String.join(",", exception.getSupportedMethods()), request.getMethod());
            log.warn(msg);
            return ResultUtil.<Void>builder().code(ErrorCode.NOT_SUPPORT_METHOD).message(msg).build();
        } else if (error instanceof ValidationException) {
            ValidationException err = (ValidationException) error;
            if (err.getCause() instanceof CodeException) {
                return ResultUtil.<Void>builder().code(((CodeException) err.getCause()).getCode()).message(err.getCause().getMessage()).build();
            } else {
                log.error("uri={} request valid param fail.", request.getRequestURI(), error);
                return ResultUtil.<Void>builder().code(ErrorCode.PARAM_ERROR).build();
            }
        } else {
            log.error("uri={} request fail.", request.getRequestURI(), error);
            return ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).build();
        }
    }
}
