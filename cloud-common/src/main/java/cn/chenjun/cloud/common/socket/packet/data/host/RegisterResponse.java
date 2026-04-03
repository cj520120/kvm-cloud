package cn.chenjun.cloud.common.socket.packet.data.host;

import cn.chenjun.cloud.common.socket.packet.data.BaseJsonData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse extends BaseJsonData {
    private boolean success;
    private String message;

    public static RegisterResponse error(String message) {
        return RegisterResponse.builder().success(false).message(message).build();
    }

    public static RegisterResponse success() {
        return RegisterResponse.builder().success(true).message("sucess").build();
    }

    public static RegisterResponse fromBytes(byte[] bytes) {
        return fromBytes(bytes, RegisterResponse.class);
    }

    public static RegisterResponse fromBuffer(ByteBuffer byteBuffer) {
        return fromBytes(byteBuffer.array(), RegisterResponse.class);
    }
}
