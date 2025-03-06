package cn.chenjun.cloud.agent.operate.process;

import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
public class DispatchFactory implements BeanPostProcessor {
    private final Map<String, Dispatch<?, ?>> dispatchMap = new HashMap<>();


    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            DispatchBind dispatchBind = method.getAnnotation(DispatchBind.class);
            if (dispatchBind != null) {
                Parameter[] parameters = method.getParameters();
                if (parameters.length != 2) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "处理命令必须为两个参数");
                }
                if (!parameters[0].getType().isAssignableFrom(Connect.class)) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "第一个参数必须为:" + Connect.class.getName());
                }
                Type paramType = parameters[1].getParameterizedType();
                Consumer<Object, Object> consumer = (connect, param) -> {
                    Object[] args = new Object[2];
                    args[0] = connect;
                    args[1] = param;
                    method.setAccessible(true);
                    try {
                        return method.invoke(bean, args);
                    } catch (InvocationTargetException err) {
                        throw (Exception) err.getCause();
                    }
                };
                Dispatch<Object, Object> dispatch = Dispatch.builder().async(dispatchBind.async()).paramType(paramType).consumer(consumer).build();
                dispatchMap.put(dispatchBind.command(), dispatch);
            }

        }
        return bean;
    }

    @SuppressWarnings({"unchecked"})
    public <K, V> Dispatch<K, V> getDispatch(String command) {
        return (Dispatch<K, V>) this.dispatchMap.get(command);
    }
}
