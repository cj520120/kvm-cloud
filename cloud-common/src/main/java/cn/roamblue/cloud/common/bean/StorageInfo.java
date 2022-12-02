package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 存储池
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * 名称
     */
    private String name;
    /**
     * 状态
     */
    private String state;
    /**
     * 容量
     */
    private long capacity;
    /**
     * 可用
     */
    private long available;
    /**
     * 已用
     */
    private long allocation;
}
