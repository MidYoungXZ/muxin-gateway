package com.muxin.gateway.core.route.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 过滤器定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterDefinition {

    private String name;

    private Map<String, String> args = new LinkedHashMap<>();

    public FilterDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            setName(text);
            return;
        }
        setName(text.substring(0, eqIdx));

        String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");
        for (int i = 0; i < args.length; i++) {
            this.args.put("_genkey_" + i, args[i]);
        }
    }

    public String[] tokenizeToStringArray(String str, String delimiters) {
        return str.split(delimiters);
    }
}
