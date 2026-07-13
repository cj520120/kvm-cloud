package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestCreateRequest {
    private String description;
    private int category;
    private int bootstrapType;
    private String bootDeviceDriver;
    private int schemeId;
    private int networkId;
    private String arch;
    private String networkDeviceDriver;
    private int bindHostId;
    private int hostId;
    private int isoTemplateId;
    private int diskTemplateId;
    private int volumeId;
    private int storageId;
    private long diskSize;
    private int groupId;
    private String hostname;
    private String password;
    private int sshId;
    private String initVendorData;

    public void validate() {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的描述信息");
        }
        if (StringUtils.isEmpty(bootDeviceDriver)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘驱动");
        }
        if (StringUtils.isEmpty(networkDeviceDriver)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择网卡驱动");
        }
        if (isoTemplateId <= 0 && diskTemplateId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择架构方案");
        }
        if (isoTemplateId > 0 && diskSize <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘大小");
        }
    }
}