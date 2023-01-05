package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadModel {
    private String storage;
    private String name;
    private String host;
    private String clientId;
    private String clientSecret;
    private String path;
}
