package cn.chenjun.cloud.management.ovn.exception;

import cn.chenjun.cloud.management.ovn.model.response.HTTPValidationError;
import lombok.Getter;

@Getter
public class OvnApiException extends RuntimeException {

    private Integer httpCode;

    private HTTPValidationError validationError;

    public OvnApiException(String message) {
        super(message);
    }

    public OvnApiException(String message, Integer httpCode) {
        super(message);
        this.httpCode = httpCode;
    }

    public OvnApiException(String message, HTTPValidationError validationError) {
        super(message);
        this.validationError = validationError;
        this.httpCode = 422;
    }

    public OvnApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public OvnApiException(String message, Integer httpCode, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
    }
}
