package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigModel {
    private Oauth2 oauth2;
    private String baseUri;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Oauth2 {
        private String title;
        private boolean enable;
    }
}
