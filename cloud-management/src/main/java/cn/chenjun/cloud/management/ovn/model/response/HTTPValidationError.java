package cn.chenjun.cloud.management.ovn.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HTTPValidationError {

    private List<ValidationError> detail;
}
