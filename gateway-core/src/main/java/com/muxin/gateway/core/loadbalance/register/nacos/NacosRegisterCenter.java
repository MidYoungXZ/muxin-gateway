package com.muxin.gateway.core.loadbalance.register.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.loadbalance.register.RegisterCenter;
import com.muxin.gateway.core.loadbalance.register.RegisterCenterListener;
import com.muxin.gateway.core.loadbalance.register.ServiceInstance;
import com.muxin.gateway.core.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/13 10:59
 */
@Data
@Slf4j
public class NacosRegisterCenter implements RegisterCenter {

    private String registerAddress;

    private String groupName = "DEFAULT_GROUP";

    private String clusterName = "DEFAULT";


    //主要用于维护服务实例信息
    private NamingService namingService;

    //主要用于维护服务定义信息
    private NamingMaintainService namingMaintainService;

    //监听器列表
    private List<RegisterCenterListener> registerCenterListenerList = new CopyOnWriteArrayList<>();


    public NacosRegisterCenter(String registerAddress, String groupName, String clusterName) {
        this.registerAddress = registerAddress;
        this.groupName = groupName;
        this.clusterName = clusterName;
        try {
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService = NamingFactory.createNamingService(registerAddress);
        } catch (NacosException e) {
            log.error("NacosRegisterCenter init failed", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void register(ServiceInstance instance) {
        try {
            //构造nacos实例信息
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(instance.getInstanceId());
            nacosInstance.setPort(instance.getPort());
            nacosInstance.setIp(instance.getHost());
            nacosInstance.setMetadata(Map.of(GatewayConstants.META_DATA_KEY, JsonUtils.toJson(instance)));
            //注册
            namingService.registerInstance(instance.serviceDefinition().getServiceId(), groupName, nacosInstance);
            //更新服务定义
            namingMaintainService.updateService(instance.serviceDefinition().getServiceId(), groupName, 0,
                    Map.of(GatewayConstants.META_DATA_KEY, JsonUtils.toJson(instance)));

            log.info("register {}", instance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deregister(ServiceInstance instance) {
        try {
            namingService.registerInstance(instance.serviceDefinition().getServiceId(),
                    groupName, instance.getHost(), instance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceInstance> selectInstances(String serviceId) {
        return List.of();
    }

    @Override
    public List<ServiceInstance> selectInstances(String serviceId, Boolean healthy) {
        return List.of();
    }

    @Override
    public void subscribe(String serviceId, RegisterCenterListener listener) {

    }


    public class NacosRegisterListener implements EventListener {


        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent namingEvent) {
                String serviceName = namingEvent.getServiceName();
                try {
                    //获取服务定义信息
                    Service service = namingMaintainService.queryService(serviceName, groupName);
                    //获取服务实例信息
                    List<Instance> allInstances = namingService.getAllInstances(service.getName(), groupName);
                    List<ServiceInstance> arrayList = new ArrayList<>();

                    for (Instance instance : allInstances) {
                        ServiceInstance serviceInstance = JsonUtils.fromJson(instance.getMetadata()
                                .get(GatewayConstants.META_DATA_KEY), ServiceInstance.class);
                        arrayList.add(serviceInstance);
                    }
                    for (RegisterCenterListener listener : registerCenterListenerList) {
                        listener.onChange(arrayList);
                    }
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
