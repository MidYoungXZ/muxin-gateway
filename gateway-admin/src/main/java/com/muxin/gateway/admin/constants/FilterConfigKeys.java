package com.muxin.gateway.admin.constants;

public final class FilterConfigKeys {
    
    private FilterConfigKeys() {}
    
    public static final String REPLENISH_RATE = "replenishRate";
    public static final String BURST_CAPACITY = "burstCapacity";
    public static final String FAILURE_RATE_THRESHOLD = "failureRateThreshold";
    public static final String WAIT_DURATION_IN_OPEN_STATE = "waitDurationInOpenState";
    public static final String RING_BUFFER_SIZE = "ringBufferSize";
    public static final String ALLOW_ORIGINS = "allowOrigins";
    public static final String ALLOW_METHODS = "allowMethods";
    public static final String ALLOW_HEADERS = "allowHeaders";
    public static final String ALLOW_CREDENTIALS = "allowCredentials";
    public static final String MAX_AGE = "maxAge";
    public static final String CONNECT_TIMEOUT = "connectTimeout";
    public static final String RESPONSE_TIMEOUT = "responseTimeout";
    public static final String PATH_REGEX = "pathRegex";
    public static final String PATH_REPLACEMENT = "pathReplacement";
    public static final String HEADERS_TO_ADD = "headersToAdd";
    public static final String HEADERS_TO_REMOVE = "headersToRemove";
    public static final String BODY_REGEX = "bodyRegex";
    public static final String BODY_REPLACEMENT = "bodyReplacement";
}