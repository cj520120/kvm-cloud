package cn.chenjun.cloud.management.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyData<T> {
    private int type;
    private int id;
    private T data;
    private long version;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NotifyData)) return false;
        NotifyData<?> that = (NotifyData<?>) o;
        return type == that.type && id == that.id && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, data);
    }
}