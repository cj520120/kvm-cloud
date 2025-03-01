package cn.chenjun.cloud.agent.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dispatch<T, V> {
    private Type paramType;
    private boolean async;
    private Consumer<T, V> consumer;

}
