package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.ConfigCreateDTO;
import com.muxin.gateway.admin.model.dto.ConfigQueryDTO;
import com.muxin.gateway.admin.model.dto.ConfigUpdateDTO;
import com.muxin.gateway.admin.model.vo.ConfigVO;
import com.muxin.gateway.admin.model.vo.PageVO;

import java.util.List;

public interface ConfigService {
    
    PageVO<ConfigVO> pageQuery(ConfigQueryDTO query);
    
    ConfigVO getByKey(String configKey);
    
    ConfigVO getDetail(Long id);
    
    Long create(ConfigCreateDTO dto);
    
    void update(Long id, ConfigUpdateDTO dto);
    
    void delete(Long id);
    
    void batchDelete(List<Long> ids);
    
    void enable(Long id);
    
    void disable(Long id);
    
    boolean checkKeyAvailable(String configKey, Long excludeId);
    
    void refreshCache();
    
    List<ConfigVO> getAllConfigs();
}