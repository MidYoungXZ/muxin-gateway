package com.muxin.gateway.admin.service;

import com.mybatisflex.core.service.IService;
import com.muxin.gateway.admin.entity.SysDept;
import com.muxin.gateway.admin.model.dto.DeptCreateDTO;
import com.muxin.gateway.admin.model.dto.DeptUpdateDTO;
import com.muxin.gateway.admin.model.vo.DeptTreeVO;
import com.muxin.gateway.admin.model.vo.DeptVO;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author muxin
 */
public interface DeptService extends IService<SysDept> {
    
    /**
     * 获取部门树
     *
     * @return 部门树
     */
    List<DeptTreeVO> getDeptTree();
    
    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    DeptVO getDeptDetail(Long id);
    
    /**
     * 创建部门
     *
     * @param dto 创建参数
     * @return 部门ID
     */
    Long createDept(DeptCreateDTO dto);
    
    /**
     * 更新部门
     *
     * @param id 部门ID
     * @param dto 更新参数
     */
    void updateDept(Long id, DeptUpdateDTO dto);
    
    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);
    
    /**
     * 启用部门
     *
     * @param id 部门ID
     */
    void enableDept(Long id);
    
    /**
     * 禁用部门
     *
     * @param id 部门ID
     */
    void disableDept(Long id);
    
    /**
     * 获取子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<DeptVO> getChildrenDepts(Long parentId);
    
    /**
     * 移动部门
     *
     * @param id 部门ID
     * @param targetParentId 目标父部门ID
     */
    void moveDept(Long id, Long targetParentId);
    
    /**
     * 检查部门名称是否可用
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @param excludeId 排除的部门ID
     * @return 是否可用
     */
    boolean checkDeptNameAvailable(String deptName, Long parentId, Long excludeId);
    
    /**
     * 检查部门编码是否可用
     *
     * @param deptCode 部门编码
     * @param excludeId 排除的部门ID
     * @return 是否可用
     */
    boolean checkDeptCodeAvailable(String deptCode, Long excludeId);
    
    /**
     * 获取部门统计信息
     *
     * @return 统计信息
     */
    Object getDeptStats();
} 