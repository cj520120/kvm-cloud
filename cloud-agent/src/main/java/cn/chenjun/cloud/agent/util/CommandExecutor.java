package cn.chenjun.cloud.agent.util;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class CommandExecutor {

    public static CommandResult executeCommand(String[] commands) {
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        int exitCode = -1;

        try {
            log.info("开始执行Shell脚本:{}", String.join(" ", commands));
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();

            // 读取输出流
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = outputReader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
            }
            outputReader.close();

            // 读取错误流
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                errorBuilder.append(line).append("\n");
            }
            errorReader.close();

            // 获取退出码
            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
           log.error("执行Shell脚本失败", e);
        }
        return CommandResult.builder().output(outputBuilder.toString()).error(errorBuilder.toString()).exitCode(exitCode).build();
    }

    @Data
    @Builder
    public static class CommandResult {
        private final String output;
        private final String error;
        private final int exitCode;

    }
}

