package com.muxin.gateway.core.plus.message;

/**
 * @projectname: muxin-gateway
 * @filename: ProtocolEnum
 * @author: yangxz
 * @data:2025/7/13 15:05
 * @description:
 */
public enum ProtocolEnum implements Protocol {
    HTTP("HTTP","1.1",true,true,false),
    LB("LB","1.0",false,false,false);

    private String code;
    private String version;
    private boolean isConnectionOriented;
    private boolean isRequestResponseBased;
    private boolean isStreamingSupported;

    ProtocolEnum(String code, String version, boolean isConnectionOriented, boolean isRequestResponseBased, boolean isStreamingSupported) {
        this.code = code;
        this.version = version;
        this.isConnectionOriented = isConnectionOriented;
        this.isRequestResponseBased = isRequestResponseBased;
        this.isStreamingSupported = isStreamingSupported;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setConnectionOriented(boolean connectionOriented) {
        isConnectionOriented = connectionOriented;
    }

    public void setRequestResponseBased(boolean requestResponseBased) {
        isRequestResponseBased = requestResponseBased;
    }

    public void setStreamingSupported(boolean streamingSupported) {
        isStreamingSupported = streamingSupported;
    }

    @Override
    public String type() {
        return getCode();
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public boolean isConnectionOriented() {
        return false;
    }

    @Override
    public boolean isRequestResponseBased() {
        return false;
    }

    @Override
    public boolean isStreamingSupported() {
        return false;
    }
}
