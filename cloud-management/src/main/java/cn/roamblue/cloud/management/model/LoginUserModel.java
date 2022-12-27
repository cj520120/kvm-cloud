package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;


/**
 * @author chenjun
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserModel {
    private Object id;
    private String type;

}
