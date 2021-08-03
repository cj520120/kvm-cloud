package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.LocaleMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
public abstract class AbstractUiService {
    @Autowired
    protected LocaleMessage localeMessage;
    @Autowired
    private ScheduledExecutorService executorService;

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


    protected <R> ResultUtil<List<ResultUtil<R>>> batchSSupplyAsync(List<Supplier<ResultUtil<R>>> suppliers) {
        List<CompletableFuture<ResultUtil<R>>> futureList = suppliers.stream().map(supplier -> CompletableFuture.supplyAsync(supplier, this.executorService)).collect(Collectors.toList());
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        List<ResultUtil<R>> resultUtilList = futureList.stream().map(future -> {
            try {
                return future.get();
            } catch (CodeException e) {
                return ResultUtil.<R>builder().code(e.getCode()).message(e.getMessage()).build();
            } catch (Exception e) {
                return ResultUtil.<R>builder().code(ErrorCode.SERVER_ERROR).message("批量执行错误:" + e.getMessage()).build();
            }
        }).collect(Collectors.toList());
        return ResultUtil.<List<ResultUtil<R>>>builder().data(resultUtilList).build();
    }
}
