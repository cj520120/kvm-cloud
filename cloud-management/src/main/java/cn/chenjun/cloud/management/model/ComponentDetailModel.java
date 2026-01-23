package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentDetailModel {
    private Integer componentId;
    private Integer componentType;
    private String componentVip;
    private String basicComponentVip;
    private Integer networkId; 
    private Date createTime;
}
