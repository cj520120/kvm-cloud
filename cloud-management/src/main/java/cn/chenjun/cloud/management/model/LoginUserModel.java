package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author chenjun
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserModel {
    private int userId;

}
