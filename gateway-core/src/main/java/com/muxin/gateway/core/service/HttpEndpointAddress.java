package com.muxin.gateway.core.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP端点地址实现
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public class HttpEndpointAddress implements EndpointAddress {

    private final String host;
    private final int port;
    private final String path;
    private final Map<String, String> parameters;
    private final Map<String, Object> metadata;
    private final String scheme;
    private final String originalUri;

    public HttpEndpointAddress(String uri) {
        this.originalUri = uri;
        this.scheme = uri.startsWith("https") ? "https" : "http";
        this.parameters = new HashMap<>();
        this.metadata = new HashMap<>();

        try {
            URI parsedUri = URI.create(uri);
            this.host = parsedUri.getHost();
            this.port = parsedUri.getPort() != -1 ? parsedUri.getPort() : ("https".equals(scheme) ? 443 : 80);
            this.path = parsedUri.getPath() != null ? parsedUri.getPath() : "/";
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI: " + uri, e);
        }
    }

    public HttpEndpointAddress(String host, int port) {
        this(host, port, "http");
    }

    public HttpEndpointAddress(String host, int port, String scheme) {
        this.host = host;
        this.port = port;
        this.path = "/";
        this.scheme = scheme != null ? scheme : "http";
        this.parameters = new HashMap<>();
        this.metadata = new HashMap<>();
        this.originalUri = scheme + "://" + host + ":" + port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }

    @Override
    public String toUri() {
        return originalUri;
    }

    @Override
    public boolean isValid() {
        return host != null && !host.isEmpty() && port > 0;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpEndpointAddress that = (HttpEndpointAddress) o;
        return port == that.port && host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return host.hashCode() * 31 + port;
    }

    @Override
    public String toString() {
        return "HttpEndpointAddress{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                ", scheme='" + scheme + '\'' +
                '}';
    }
}
