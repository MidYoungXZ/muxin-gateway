-- 插入示例路由配置（将现有的静态配置迁移到数据库）
MERGE INTO gateway_route (id, uri, predicates, filters, metadata, order_num, enabled, version) VALUES
('user-service', 'http://localhost:8081', 
 '[{"name":"Path","args":{"pattern":"/api/user/**"}},{"name":"Method","args":{"methods":"GET,POST,PUT,DELETE"}}]',
 '[{"name":"StripPrefix","args":{"parts":"2"}}]',
 '{"description":"用户服务路由","version":"1.0","group":"用户服务"}',
 1, 1, 1),

('order-service', 'http://localhost:8082',
 '[{"name":"Path","args":{"pattern":"/api/order/**"}},{"name":"Method","args":{"methods":"GET,POST,PUT,DELETE"}}]',
 '[{"name":"StripPrefix","args":{"parts":"2"}}]',
 '{"description":"订单服务路由","version":"1.0","group":"订单服务"}',
 2, 1, 1),

('product-service', 'http://localhost:8083',
 '[{"name":"Path","args":{"pattern":"/api/product/**"}},{"name":"Method","args":{"methods":"GET,POST,PUT,DELETE"}}]',
 '[{"name":"StripPrefix","args":{"parts":"2"}}]',
 '{"description":"商品服务路由","version":"1.0","group":"产品服务"}',
 3, 1, 1),

('test-httpbin', 'http://httpbin.org',
 '[{"name":"Path","args":{"pattern":"/test/**"}}]',
 '[{"name":"StripPrefix","args":{"parts":"1"}}]',
 '{"description":"httpbin测试服务路由","version":"1.0","group":"测试服务"}',
 10, 1, 1);

-- 插入系统配置
MERGE INTO gateway_config (id,config_key, config_value, description) VALUES
(1,'sync.interval', '30000', '路由配置同步间隔（毫秒）'),
(2,'route.cache.enabled', 'true', '是否启用路由缓存'),
(3,'audit.log.retention.days', '30', '审计日志保留天数');

