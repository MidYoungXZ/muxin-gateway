package com.muxin.gateway.core.route;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PredicateDefinition {

    private String name;

    private Map<String, String> args = new LinkedHashMap<>();

    public PredicateDefinition() {}

    public PredicateDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            setName(text);
            return;
        }
        
        setName(text.substring(0, eqIdx));
        
        String[] args = StringUtils.tokenizeToStringArray(text.substring(eqIdx + 1), ",");
        for (int i = 0; i < args.length; i++) {
            this.args.put("_genkey_" + i, args[i]);
        }
    }

} 