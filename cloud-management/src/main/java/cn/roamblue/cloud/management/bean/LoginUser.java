package cn.roamblue.cloud.management.bean;

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
public class LoginUser {
    private Object id;
    private String type;
    private Collection<String> authorities;

}
