package com.roamblue.cloud.common.error;

import com.roamblue.cloud.common.bean.ResultUtil;

public class CodeException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private int code;

    public CodeException(ResultUtil resultUtil) {
        this(resultUtil.getCode(), resultUtil.getMessage());
    }

    public CodeException(int code) {
        super();
        this.code = code;
    }

    public CodeException(int code, String message) {
        super(message);
        this.code = code;
    }


    public CodeException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }


    public CodeException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    protected CodeException(int code, String message, Throwable cause,
                            boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
