package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestQmaRequest {
    /**
     * 虚拟机名称
     */
    private String name;
    private int qmaExecuteTimeout;
    private int qmaCheckTimeout;
    private List<QmaBody> commands;

    public static class QmaType {
        public static final int WRITE_FILE = 0;
        public static final int EXECUTE = 1;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QmaBody {
        private int index;
        private int command;
        private String data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WriteFile {
        private String fileName;
        private String fileBody;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Execute {
        private String command;
        @Builder.Default
        private boolean checkSuccess = true;
        private String[] args;
    }

}
