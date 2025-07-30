-- 过滤器管理初始化数据
-- 插入系统内置过滤器

-- 清空现有数据
DELETE FROM `gw_filter` WHERE `is_system` = 1;

-- 插入系统内置过滤器
INSERT INTO `gw_filter` (`filter_name`, `filter_type`, `description`, `config`, `order`, `is_system`, `enabled`, `create_time`, `update_time`, `create_by`) VALUES
-- 请求头处理过滤器
('添加请求头-Gateway标识', 'AddRequestHeader', '向请求中添加网关标识头', '{"name": "X-Gateway-From", "value": "muxin-gateway"}', 10, 1, 1, NOW(), NOW(), 'system'),
('添加请求头-请求时间', 'AddRequestHeader', '向请求中添加请求时间戳', '{"name": "X-Request-Time", "value": "#{T(System).currentTimeMillis()}"}', 11, 1, 1, NOW(), NOW(), 'system'),
('添加请求头-请求ID', 'AddRequestHeader', '向请求中添加唯一请求ID', '{"name": "X-Request-ID", "value": "#{T(java.util.UUID).randomUUID().toString()}"}', 12, 1, 1, NOW(), NOW(), 'system'),
('移除请求头-内部标识', 'RemoveRequestHeader', '移除内部使用的请求头', '{"name": "X-Internal-Token"}', 13, 1, 1, NOW(), NOW(), 'system'),

-- 响应头处理过滤器
('添加响应头-CORS', 'AddResponseHeader', '添加跨域响应头', '{"name": "Access-Control-Allow-Origin", "value": "*"}', 20, 1, 1, NOW(), NOW(), 'system'),
('添加响应头-安全策略', 'AddResponseHeader', '添加安全策略响应头', '{"name": "X-Content-Type-Options", "value": "nosniff"}', 21, 1, 1, NOW(), NOW(), 'system'),
('添加响应头-缓存控制', 'AddResponseHeader', '添加缓存控制响应头', '{"name": "Cache-Control", "value": "no-cache, no-store, must-revalidate"}', 22, 1, 1, NOW(), NOW(), 'system'),
('移除响应头-服务器信息', 'RemoveResponseHeader', '移除服务器信息响应头', '{"name": "Server"}', 23, 1, 1, NOW(), NOW(), 'system'),

-- 路径处理过滤器
('路径重写-API版本', 'RewritePath', '重写API版本路径', '{"regexp": "/api/v1/(?<segment>.*)", "replacement": "/api/${segment}"}', 30, 1, 1, NOW(), NOW(), 'system'),
('路径重写-移除前缀', 'RewritePath', '移除路径前缀', '{"regexp": "/gateway/(?<segment>.*)", "replacement": "/${segment}"}', 31, 1, 1, NOW(), NOW(), 'system'),
('前缀剥离-单级', 'StripPrefix', '剥离路径前缀（单级）', '{"parts": 1}', 32, 1, 1, NOW(), NOW(), 'system'),
('前缀剥离-双级', 'StripPrefix', '剥离路径前缀（双级）', '{"parts": 2}', 33, 1, 1, NOW(), NOW(), 'system'),

-- 限流过滤器
('请求限流-基础', 'RequestRateLimiter', '基础请求限流配置', '{"replenishRate": 10, "burstCapacity": 20, "requestedTokens": 1}', 40, 1, 1, NOW(), NOW(), 'system'),
('请求限流-严格', 'RequestRateLimiter', '严格请求限流配置', '{"replenishRate": 5, "burstCapacity": 10, "requestedTokens": 1}', 41, 1, 1, NOW(), NOW(), 'system'),
('请求限流-宽松', 'RequestRateLimiter', '宽松请求限流配置', '{"replenishRate": 50, "burstCapacity": 100, "requestedTokens": 1}', 42, 1, 1, NOW(), NOW(), 'system'),

-- 熔断过滤器
('熔断器-基础', 'CircuitBreaker', '基础熔断器配置', '{"name": "defaultCircuitBreaker", "fallbackUri": "/fallback"}', 50, 1, 1, NOW(), NOW(), 'system'),
('熔断器-敏感', 'CircuitBreaker', '敏感服务熔断器', '{"name": "sensitiveCircuitBreaker", "fallbackUri": "/sensitive-fallback", "failureRateThreshold": 20}', 51, 1, 1, NOW(), NOW(), 'system'),
('熔断器-稳定', 'CircuitBreaker', '稳定服务熔断器', '{"name": "stableCircuitBreaker", "fallbackUri": "/stable-fallback", "failureRateThreshold": 80}', 52, 1, 1, NOW(), NOW(), 'system'),

-- 重试过滤器
('重试-GET请求', 'Retry', 'GET请求重试配置', '{"retries": 3, "statuses": ["BAD_GATEWAY"], "methods": ["GET"], "backoff": {"firstBackoff": "10ms", "maxBackOff": "50ms", "factor": 2}}', 60, 1, 1, NOW(), NOW(), 'system'),
('重试-幂等请求', 'Retry', '幂等请求重试配置', '{"retries": 2, "statuses": ["BAD_GATEWAY", "GATEWAY_TIMEOUT"], "methods": ["GET", "PUT", "DELETE"]}', 61, 1, 1, NOW(), NOW(), 'system'),
('重试-所有请求', 'Retry', '所有请求重试配置', '{"retries": 1, "statuses": ["BAD_GATEWAY"]}', 62, 1, 1, NOW(), NOW(), 'system'),

-- 修改请求过滤器
('修改请求体', 'ModifyRequestBody', '修改请求体内容', '{"contentType": "application/json"}', 70, 1, 0, NOW(), NOW(), 'system'),
('修改响应体', 'ModifyResponseBody', '修改响应体内容', '{"contentType": "application/json"}', 71, 1, 0, NOW(), NOW(), 'system'),

-- 日志过滤器
('请求日志', 'RequestLogging', '记录请求日志', '{"includeHeaders": true, "includeQueryParams": true, "includePayload": false}', 80, 1, 1, NOW(), NOW(), 'system'),
('响应日志', 'ResponseLogging', '记录响应日志', '{"includeHeaders": true, "includePayload": false}', 81, 1, 1, NOW(), NOW(), 'system'),

-- 安全过滤器
('XSS防护', 'XSSProtection', 'XSS攻击防护', '{"enabled": true, "mode": "block"}', 90, 1, 1, NOW(), NOW(), 'system'),
('SQL注入防护', 'SQLInjectionProtection', 'SQL注入攻击防护', '{"enabled": true, "strictMode": false}', 91, 1, 1, NOW(), NOW(), 'system'),
('IP白名单', 'IPWhitelist', 'IP地址白名单过滤', '{"allowedIPs": ["127.0.0.1", "192.168.1.0/24"]}', 92, 1, 0, NOW(), NOW(), 'system'),
('IP黑名单', 'IPBlacklist', 'IP地址黑名单过滤', '{"blockedIPs": []}', 93, 1, 0, NOW(), NOW(), 'system'),

-- 缓存过滤器
('响应缓存', 'ResponseCache', '响应结果缓存', '{"timeToLive": "300s", "cacheKeyResolver": "#{request.getURI()}"}', 100, 1, 0, NOW(), NOW(), 'system'),
('本地缓存', 'LocalCache', '本地缓存过滤器', '{"maximumSize": 1000, "expireAfterWrite": "10m"}', 101, 1, 0, NOW(), NOW(), 'system'),

-- 监控过滤器
('性能监控', 'PerformanceMonitor', '性能指标监控', '{"enabled": true, "includeRequestHeaders": false}', 110, 1, 1, NOW(), NOW(), 'system'),
('访问统计', 'AccessStatistics', '访问统计收集', '{"enabled": true, "sampleRate": 1.0}', 111, 1, 1, NOW(), NOW(), 'system'),

-- 转换过滤器
('JSON转XML', 'JsonToXml', 'JSON到XML格式转换', '{"rootName": "root", "prettyPrint": true}', 120, 1, 0, NOW(), NOW(), 'system'),
('XML转JSON', 'XmlToJson', 'XML到JSON格式转换', '{"includeRoot": false, "prettyPrint": true}', 121, 1, 0, NOW(), NOW(), 'system');

-- 验证插入结果
SELECT 
    id,
    filter_name,
    filter_type,
    description,
    `order`,
    is_system,
    enabled,
    create_time
FROM `gw_filter` 
WHERE `is_system` = 1 
ORDER BY `order`, `create_time`;

-- 输出统计信息
SELECT 
    filter_type,
    COUNT(*) as count,
    SUM(CASE WHEN enabled = 1 THEN 1 ELSE 0 END) as enabled_count
FROM `gw_filter` 
WHERE `is_system` = 1 
GROUP BY filter_type 
ORDER BY filter_type; 