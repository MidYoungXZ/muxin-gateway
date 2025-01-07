package com.muxin.gateway.core.filter;

import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class FilterDefinition {
    private String name;
    private Map<String, String> args = new LinkedHashMap<>();
    
    public FilterDefinition() {}
    
    public FilterDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            setName(text);
            return;
        }
        setName(text.substring(0, eqIdx));
    }
}
