package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.UserCreateDTO;
import com.muxin.gateway.admin.model.dto.UserQueryDTO;
import com.muxin.gateway.admin.model.dto.UserUpdateDTO;
import com.muxin.gateway.admin.model.dto.PasswordUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author muxin
 */
public interface UserService {
    
    /**
     * 分页查询用户列表
     */
    PageVO<UserVO> pageQuery(UserQueryDTO query);
    
    /**
     * 获取用户详情
     */
    UserVO getUserDetail(Long id);
    
    /**
     * 根据用户名获取用户
     */
    UserVO getByUsername(String username);
    
    /**
     * 创建用户
     */
    Long createUser(UserCreateDTO dto);
    
    /**
     * 更新用户
     */
    void updateUser(Long id, UserUpdateDTO dto);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 批量删除用户
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 启用用户
     */
    void enableUser(Long id);
    
    /**
     * 禁用用户
     */
    void disableUser(Long id);
    
    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);
    
    /**
     * 修改密码
     */
    void updatePassword(Long id, PasswordUpdateDTO dto);
    
    /**
     * 分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 获取用户的角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);
} 