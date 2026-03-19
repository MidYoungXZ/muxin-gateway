package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuthFilter extends AbstractFilter {

    public static final String TYPE = "AUTH";

    private String authType;
    private String secretKey;
    private String headerName;

    public AuthFilter() {
        this.authType = "NONE";
        this.headerName = "Authorization";
    }

    public AuthFilter(FilterDefinition definition) {
        this.name = TYPE;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
        this.authType = definition.getStringConfig("auth-type", "NONE");
        this.secretKey = definition.getStringConfig("secret-key", "muxin-gateway-secret");
        this.headerName = definition.getStringConfig("header-name", "Authorization");
    }

    @Override
    protected void doFilter(HttpServerExchange exchange, FilterChain chain) {
        String authHeader = exchange.request().headers().get(headerName);

        if ("NONE".equalsIgnoreCase(authType)) {
            chain.filter(exchange, chain);
            return;
        }

        if (authHeader == null || authHeader.isEmpty()) {
            logWarn("Missing authorization header");
            sendUnauthorized(exchange, "Missing authorization header");
            return;
        }

        boolean valid = false;

        if ("JWT".equalsIgnoreCase(authType)) {
            valid = validateJwt(authHeader);
        } else if ("BASIC".equalsIgnoreCase(authType)) {
            valid = validateBasic(authHeader);
        } else if ("TOKEN".equalsIgnoreCase(authType)) {
            valid = validateToken(authHeader);
        }

        if (valid) {
            chain.filter(exchange, chain);
        } else {
            logWarn("Invalid authorization for type: {}", authType);
            sendUnauthorized(exchange, "Invalid authorization");
        }
    }

    private boolean validateJwt(String authHeader) {
        try {
            if (authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            String[] parts = authHeader.split("\\.");
            if (parts.length != 3) {
                logWarn("Invalid JWT format");
                return false;
            }

            String signatureInput = parts[0] + "." + parts[1];
            String expectedSignature = signHS256(signatureInput, secretKey);

            if (!expectedSignature.equals(parts[2])) {
                logWarn("Invalid JWT signature");
                return false;
            }

            logDebug("JWT validation successful");
            return true;

        } catch (Exception e) {
            logError("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    private String signHS256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    }

    private boolean validateBasic(String authHeader) {
        try {
            if (!authHeader.startsWith("Basic ")) {
                return false;
            }

            String encoded = authHeader.substring(6);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            String decodedStr = new String(decoded, StandardCharsets.UTF_8);

            int colonIndex = decodedStr.indexOf(':');
            if (colonIndex < 0) {
                return false;
            }

            String username = decodedStr.substring(0, colonIndex);
            String password = decodedStr.substring(colonIndex + 1);

            logDebug("Basic auth for user: {}", username);
            return true;

        } catch (Exception e) {
            logError("Basic auth validation error: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateToken(String authHeader) {
        if (authHeader.startsWith("Token ")) {
            authHeader = authHeader.substring(6);
        }
        return authHeader != null && !authHeader.isEmpty() && authHeader.length() >= 10;
    }

    private void sendUnauthorized(HttpServerExchange exchange, String message) {
        exchange.response().setStatus(HttpResponseStatus.UNAUTHORIZED);
        exchange.response().header(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json");
        exchange.setResponseBody(String.format(
                "{\"error\":{\"code\":401,\"message\":\"%s\"}}", message));
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new AuthFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
        }
    }
}