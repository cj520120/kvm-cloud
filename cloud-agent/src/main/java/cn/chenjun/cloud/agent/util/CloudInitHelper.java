package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.thread.ThreadUtil;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.Error;
import org.libvirt.LibvirtException;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class CloudInitHelper {
    private static final String CLOUD_INIT_FINISHED_FILE = "/var/lib/cloud/instance/boot-finished";
    private static final String CLOUD_INIT_FINISH = "done";
    private static final String CLOUD_INIT_PENDING = "pending";
    private static final String CHECK_CLOUD_INIT_CMD = String.format("test -f %s && echo %s || echo %s", CLOUD_INIT_FINISHED_FILE, CLOUD_INIT_FINISH, CLOUD_INIT_PENDING);

    /**
     * 等待虚拟机 CloudInit 执行完成
     * 流程：1. 等待 QGA 就绪并支持 guest-exec 命令 2. 执行检测命令 3. 轮询检测结果直到完成/超时
     *
     * @param connect           libvirt 连接
     * @param name              虚拟机名称
     * @param maxTimeoutSeconds 最大等待超时时间（秒）
     */
    public static void waitCloudInit(Connect connect, String name, int maxTimeoutSeconds) {
        maxTimeoutSeconds = Math.max(60, maxTimeoutSeconds);
        long deadlineTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxTimeoutSeconds);
        while (System.currentTimeMillis() < deadlineTime) {
            try {
                ThreadUtil.safeSleep(5000);
                Domain domain = DomainUtil.findDomainByName(connect, name);
                if (domain == null) {
                    log.warn("虚拟机已经关闭，退出检测...");
                    throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机已经关闭");
                }
                if (waitForQgaReady(domain, QgaCommand.GUEST_EXEC, maxTimeoutSeconds)) {
                    log.info("QGA 支持 guest-exec 命令，开始检测CloudInit完成状态...");
                    if (checkCloudInitFinished(domain, deadlineTime)) {
                        log.info("CloudInit检测进程完成，退出...");
                        return;
                    } else {
                        log.warn("CloudInit检测进程未完成，将在5秒后重试...");
                    }
                } else {
                    log.warn("QGA 不支持 guest-exec 命令，等待中...");
                }
            } catch (CodeException e) {
                throw e;
            } catch (Exception e) {
                log.warn("等待CloudInit完成失败，将在5秒后重试...");
            }
        }
        throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "等待CloudInit完成超时");
    }

    private static <T> T executeCommand(Domain domain, QgaCommandRequest request, int timeoutSeconds, Class<T> returnType) throws LibvirtException {
        String commandJson = GsonBuilderUtil.create().toJson(request);
        String responseJson = domain.qemuAgentCommand(commandJson, timeoutSeconds, 0);
        if (returnType == null) {
            return null;
        }
        Map<String, Object> response = GsonBuilderUtil.create().fromJson(responseJson, Map.class);
        if (response.containsKey("error")) {
            String errorDesc = Optional.ofNullable(response.get("error")).map(obj -> GsonBuilderUtil.create().toJson(obj)).orElse("未知错误");
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "QGA命令执行失败：" + errorDesc);
        }

        if (!response.containsKey("return")) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "无效的返回值");
        }
        return GsonBuilderUtil.create().fromJson(GsonBuilderUtil.create().toJson(response.get("return")), returnType);
    }

    /**
     * 执行 CloudInit 完成状态检测命令，返回进程ID
     */
    private static int executeCloudInitCheckCommand(Domain domain, int maxTimeoutSeconds) throws LibvirtException {
        List<String> cmdArgs = Arrays.asList("-c", CHECK_CLOUD_INIT_CMD);
        GuestExecArguments execArgs = new GuestExecArguments("sh", cmdArgs, true);
        QgaCommandRequest execRequest = new QgaCommandRequest(QgaCommand.GUEST_EXEC, execArgs);
        return executeCommand(domain, execRequest, maxTimeoutSeconds, GuestExecReturn.class).getPid();
    }

    private static String parseGuestExecOutput(String output) {
        if (ObjectUtils.isEmpty(output)) {
            return "";
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(output);
            return new String(decodedBytes, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, String.format("解析base64数据错误：%s，原始数据：%s", e.getMessage(), output));
        }
    }

    /**
     * 轮询检测进程状态，直到完成/超时
     */
    private static boolean checkCloudInitFinished(Domain domain, long deadline) throws LibvirtException {

        int pid = executeCloudInitCheckCommand(domain, 30);
        GuestExecStatusArguments statusArgs = new GuestExecStatusArguments(pid);
        QgaCommandRequest statusRequest = new QgaCommandRequest(QgaCommand.GUEST_EXEC_STATUS, statusArgs);
        while (System.currentTimeMillis() < deadline) {
            GuestExecStatusReturn execStatusReturn = executeCommand(domain, statusRequest, 30, GuestExecStatusReturn.class);
            if (Objects.isNull(execStatusReturn.getExited()) || !execStatusReturn.getExited()) {
                log.info("CloudInit检测进程[{}]正在运行，等待中...", pid);
                ThreadUtil.safeSleep(5000);
            } else {
                log.info("CloudInit检测进程[{}]已退出，状态：{}", pid, execStatusReturn);
                if (ObjectUtils.isEmpty(execStatusReturn.getOutput())) {
                    throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "CloudInit检测进程已退出，但无输出数据");
                }
                if (execStatusReturn.getExitCode() != 0) {
                    String errorMsg = parseGuestExecOutput(execStatusReturn.getError());
                    throw new CodeException(ErrorCode.VM_COMMAND_ERROR, String.format("CloudInit检测进程[%d]退出码异常：%d，错误内容:%s", pid, execStatusReturn.getExitCode(), errorMsg));
                }
                String output = parseGuestExecOutput(execStatusReturn.getOutput());
                if (Objects.equals(output, CLOUD_INIT_FINISH)) {
                    return true;
                } else if (Objects.equals(output, CLOUD_INIT_PENDING)) {
                    log.info("CloudInit检测进程[{}]输出：{}，继续等待...", pid, output);
                    ThreadUtil.safeSleep(5000);
                }
                break;
            }
        }
        return false;
    }

    /**
     * 等待 QGA 就绪并检查指定命令是否可用
     */
    private static boolean waitForQgaReady(Domain domain, String requiredCommand, int timeoutSeconds) throws LibvirtException {
        try {
            executeCommand(domain, QgaCommandRequest.builder().execute(QgaCommand.GUEST_PING).build(), timeoutSeconds, null);
            GuestInfoReturn info = executeCommand(domain, QgaCommandRequest.builder().execute(QgaCommand.GUEST_INFO).build(), timeoutSeconds, GuestInfoReturn.class);
            boolean isSupported = false;
            if (!ObjectUtils.isEmpty(info.getSupportedCommands())) {
                isSupported = info.getSupportedCommands().stream().anyMatch(command -> Objects.equals(command.getName(), requiredCommand) && command.isEnabled());
            }
            if (isSupported) {
                log.info("命令[{}]已启用，继续执行...", requiredCommand);
                return true;
            } else {
                log.warn("命令[{}]暂未启用，等待中...", requiredCommand);
            }
        } catch (LibvirtException e) {
            if (e.getError().getCode() == Error.ErrorNumber.VIR_ERR_NO_DOMAIN) {
                throw e;
            }
            log.warn("QGA未响应，等待中...");
        }
        return false;
    }

    public static class QgaCommand {
        public static final String GUEST_EXEC = "guest-exec";
        public static final String GUEST_EXEC_STATUS = "guest-exec-status";
        public static final String GUEST_INFO = "guest-info";
        public static final String GUEST_PING = "guest-ping";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class QgaCommandRequest {
        @SerializedName("execute")
        private String execute;
        @SerializedName("arguments")
        private Object arguments;
    }

    /**
     * guest-exec 命令的参数结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class GuestExecArguments {
        @SerializedName("path")
        private String path;
        @SerializedName("arg")
        private List<String> arg;
        @SerializedName("capture-output")
        private boolean captureOutput;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class GuestExecStatusArguments {
        private int pid;
    }

    @Data
    @NoArgsConstructor
    private static class GuestExecReturn {
        @SerializedName("pid")
        private int pid;
    }

    @Data
    @NoArgsConstructor
    private static class GuestExecStatusReturn {
        @SerializedName("exitcode")
        private Integer exitCode;
        @SerializedName("out-data")
        private String output;
        @SerializedName("err-data")
        private String error;
        @SerializedName("exited")
        private Boolean exited;
    }

    @Data
    @NoArgsConstructor
    private static class GuestInfoReturn {
        @SerializedName("supported_commands")
        private List<GuestCommand> supportedCommands;
        @SerializedName("version")
        private String version;

    }

    /**
     * 支持的命令信息
     */
    @Data
    @NoArgsConstructor
    private static class GuestCommand {
        @SerializedName("name")
        private String name;
        @SerializedName("enabled")
        private boolean enabled;
        @SerializedName("success-response")
        private boolean successResponse;
    }

}