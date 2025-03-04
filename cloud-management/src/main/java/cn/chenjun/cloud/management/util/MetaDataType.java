package cn.chenjun.cloud.management.util;

public enum MetaDataType {
    CLOUD("#cloud-config\n", "text/cloud-config");
    private final String firstLine;
    private final String contextType;

    MetaDataType(String firstLine, String contextType) {
        this.firstLine = firstLine;
        this.contextType = contextType;
    }

    public String getContextType() {
        return contextType;
    }

    public String getFirstLine() {
        return firstLine;
    }
}
