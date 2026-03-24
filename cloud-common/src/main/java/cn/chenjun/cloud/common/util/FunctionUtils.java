package cn.chenjun.cloud.common.util;

import java.util.concurrent.Callable;

public class FunctionUtils {
    public static <T> T ignoreErrorCall(Callable<T> callable ){
        try {
            return callable.call();
        }catch (Exception e){
            return null;
        }
    }
}
