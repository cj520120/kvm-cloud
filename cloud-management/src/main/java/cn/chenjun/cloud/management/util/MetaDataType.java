package cn.chenjun.cloud.management.util;

import lombok.Getter;

@Getter
public enum MetaDataType {
    CLOUD("#cloud-config\n", "text/cloud-config");
    private final String firstLine;
    private final String contextType;

    MetaDataType(String firstLine, String contextType) {
        this.firstLine = firstLine;
        this.contextType = contextType;
    }

}
