package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * @author chenjun
 */
public class SchemeModifyRequest {
    private int schemeId;
    private String name;
    private int cpu;
    private long memory;
    private int share;
    private int sockets;
    private int cores;
    private int threads;

    public int getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(int schemeId) {
        this.schemeId = schemeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public int getShare() {
        return share;
    }

    public void setShare(int share) {
        this.share = share;
    }

    public int getSockets() {
        return sockets;
    }

    public void setSockets(int sockets) {
        this.sockets = sockets;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void validate() {
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的方案ID");
        }
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入方案名称");
        }
        if (cpu <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "CPU数量必须大于0");
        }
        if (memory <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "内存大小必须大于0");
        }

    }
}