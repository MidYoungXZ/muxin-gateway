package com.muxin.gateway.core.plus.protocol.message.http;

import com.muxin.gateway.core.plus.protocol.message.MessageHeaders;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @projectname: muxin-gateway
 * @filename: HttpMessageHeaders
 * @author: yangxz
 * @data:2025/7/15 22:52
 * @description:
 */
public class HttpMessageHeaders implements MessageHeaders {


    @Override
    public void set(String name, Object value) {

    }

    @Override
    public <T> T get(String name, Class<T> type) {
        return null;
    }

    @Override
    public <T> Optional<T> getOptional(String name, Class<T> type) {
        return Optional.empty();
    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    public void remove(String name) {

    }

    @Override
    public Set<String> getNames() {
        return Set.of();
    }

    @Override
    public Map<String, Object> asMap() {
        return Map.of();
    }

}
