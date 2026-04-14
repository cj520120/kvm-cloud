package cn.chenjun.cloud.management.ovn.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    private List<Object> loc;

    private String msg;

    private String type;

    private Object input;

    private Object ctx;
}
