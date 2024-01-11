package cn.chenjun.cloud.agent.operate.bean;

import lombok.Data;

import java.util.List;

/**
 * @author chenjun
 */
@Data
public class DomainContext {
    private String path;
    private String domain;
    private String machine;
    private String arch;
    private String firmware;
    private Loader loader;

    @Data
    public static class Loader {
        private boolean supported;
        private String path;
        private List<String> type;
        private boolean isInstall;
        private boolean secure;
    }
}
