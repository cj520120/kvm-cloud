package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;

import java.util.concurrent.Callable;

/**
 * @author chenjun
 */
public abstract class AbstractUiService {
    protected <T> ResultUtil<T> call(Callable<T> callable) {
        try {
            return ResultUtil.<T>builder().data(callable.call()).build();
        } catch (CodeException e) {
            return ResultUtil.<T>builder().code(e.getCode()).message(e.getMessage()).build();
        } catch (Exception e) {
            return ResultUtil.<T>builder().code(ErrorCode.SERVER_ERROR).message(e.getMessage()).build();
        }
    }

    protected <T> ResultUtil<T> call(Runnable callable) {
        try {
            callable.run();
            return ResultUtil.<T>builder().build();
        } catch (CodeException e) {
            return ResultUtil.<T>builder().code(e.getCode()).message(e.getMessage()).build();
        } catch (Exception e) {
            return ResultUtil.<T>builder().code(ErrorCode.SERVER_ERROR).message(e.getMessage()).build();
        }
    }

}
