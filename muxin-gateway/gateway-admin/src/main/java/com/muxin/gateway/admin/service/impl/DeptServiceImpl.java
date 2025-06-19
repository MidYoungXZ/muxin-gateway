package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.SysDept;
import com.muxin.gateway.admin.entity.SysUser;
import static com.muxin.gateway.admin.entity.table.Tables.*;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.DeptMapper;
import com.muxin.gateway.admin.mapper.UserMapper;
import com.muxin.gateway.admin.model.dto.DeptCreateDTO;
import com.muxin.gateway.admin.model.dto.DeptUpdateDTO;
import com.muxin.gateway.admin.model.vo.DeptTreeVO;
import com.muxin.gateway.admin.model.vo.DeptVO;
import com.muxin.gateway.admin.service.DeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author muxin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, SysDept> implements DeptService {
    
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    
    @Override
    public List<DeptTreeVO> getDeptTree() {
        // 查询所有部门
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DELETED.eq(0))
                .orderBy(SYS_DEPT.ORDER_NUM.asc(), SYS_DEPT.CREATE_TIME.asc());
        
        List<SysDept> deptList = list(wrapper);
        
        // 转换为树形结构
        List<DeptTreeVO> treeList = buildTree(deptList, 0L);
        return treeList;
    }
    
    @Override
    public DeptVO getDeptDetail(Long id) {
        SysDept dept = getById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }
        
        return convertToVO(dept);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDept(DeptCreateDTO dto) {
        // 检查部门名称是否重复
        QueryWrapper checkWrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DEPT_NAME.eq(dto.getDeptName()))
                .and(SYS_DEPT.DELETED.eq(0));
        
        if (count(checkWrapper) > 0) {
            throw new BusinessException("部门名称已存在");
        }
        
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        
        // 设置祖级列表
        if (dto.getParentId() == null || dto.getParentId() == 0) {
            dept.setParentId(0L);
            dept.setAncestors("0");
        } else {
            SysDept parent = getById(dto.getParentId());
            if (parent == null || parent.getDeleted() == 1) {
                throw new BusinessException("父部门不存在");
            }
            dept.setAncestors(parent.getAncestors() + "," + parent.getId());
        }
        
        dept.setStatus(1);
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        dept.setCreateBy(StpUtil.getLoginIdAsString());
        dept.setDeleted(0);
        
        save(dept);
        
        log.info("创建部门成功：{}", dept.getDeptName());
        return dept.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(Long id, DeptUpdateDTO dto) {
        SysDept dept = getById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }
        
        // 检查部门名称是否重复
        QueryWrapper checkWrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DEPT_NAME.eq(dto.getDeptName()))
                .and(SYS_DEPT.ID.ne(id))
                .and(SYS_DEPT.DELETED.eq(0));
        
        if (count(checkWrapper) > 0) {
            throw new BusinessException("部门名称已存在");
        }
        
        // 如果修改了父部门，需要更新祖级列表
        if (dto.getParentId() != null && !dto.getParentId().equals(dept.getParentId())) {
            updateAncestors(id, dto.getParentId());
        }
        
        BeanUtils.copyProperties(dto, dept);
        dept.setUpdateTime(LocalDateTime.now());
        dept.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(dept);
        
        log.info("更新部门成功：{}", dept.getDeptName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        SysDept dept = getById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }
        
        // 检查是否有子部门
        QueryWrapper childWrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.PARENT_ID.eq(id))
                .and(SYS_DEPT.DELETED.eq(0));
        
        if (count(childWrapper) > 0) {
            throw new BusinessException("存在子部门，不能删除");
        }
        
        // 检查是否有用户
        QueryWrapper userWrapper = QueryWrapper.create()
                .select()
                .from(SYS_USER)
                .where(SYS_USER.DEPT_ID.eq(id))
                .and(SYS_USER.DELETED.eq(0));
        
        if (userMapper.selectCountByQuery(userWrapper) > 0) {
            throw new BusinessException("部门下存在用户，不能删除");
        }
        
        // 逻辑删除
        dept.setDeleted(1);
        dept.setUpdateTime(LocalDateTime.now());
        dept.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(dept);
        
        log.info("删除部门成功：{}", dept.getDeptName());
    }
    
    @Override
    public void enableDept(Long id) {
        updateStatus(id, 1);
    }
    
    @Override
    public void disableDept(Long id) {
        updateStatus(id, 0);
    }
    
    @Override
    public List<DeptVO> getChildrenDepts(Long parentId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.PARENT_ID.eq(parentId))
                .and(SYS_DEPT.DELETED.eq(0))
                .orderBy(SYS_DEPT.ORDER_NUM.asc(), SYS_DEPT.CREATE_TIME.asc());
        
        List<SysDept> deptList = list(wrapper);
        return deptList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDept(Long id, Long targetParentId) {
        SysDept dept = getById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }
        
        // 不能移动到自己或子部门下
        if (id.equals(targetParentId)) {
            throw new BusinessException("不能移动到自己下面");
        }
        
        if (targetParentId != 0 && isChildDept(id, targetParentId)) {
            throw new BusinessException("不能移动到子部门下面");
        }
        
        // 更新祖级列表
        updateAncestors(id, targetParentId);
        
        log.info("移动部门成功：{} -> {}", dept.getDeptName(), targetParentId);
    }
    
    @Override
    public boolean checkDeptNameAvailable(String deptName, Long parentId, Long excludeId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DEPT_NAME.eq(deptName))
                .and(SYS_DEPT.PARENT_ID.eq(parentId))
                .and(SYS_DEPT.DELETED.eq(0));
        
        if (excludeId != null) {
            wrapper.and(SYS_DEPT.ID.ne(excludeId));
        }
        
        long count = count(wrapper);
        return count == 0;
    }
    
    @Override
    public boolean checkDeptCodeAvailable(String deptCode, Long excludeId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DEPT_CODE.eq(deptCode))
                .and(SYS_DEPT.DELETED.eq(0));
        
        if (excludeId != null) {
            wrapper.and(SYS_DEPT.ID.ne(excludeId));
        }
        
        long count = count(wrapper);
        return count == 0;
    }
    
    @Override
    public Object getDeptStats() {
        // 统计总部门数
        long totalCount = count(QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DELETED.eq(0)));
        
        // 统计启用部门数
        long enabledCount = count(QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.DELETED.eq(0))
                .and(SYS_DEPT.STATUS.eq(1)));
        
        // 统计禁用部门数
        long disabledCount = totalCount - enabledCount;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("enabledCount", enabledCount);
        stats.put("disabledCount", disabledCount);
        
        return stats;
    }
    
    /**
     * 更新状态
     */
    private void updateStatus(Long id, Integer status) {
        SysDept dept = getById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException("部门不存在");
        }
        
        dept.setStatus(status);
        dept.setUpdateTime(LocalDateTime.now());
        dept.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(dept);
        
        log.info("更新部门状态成功：{}，状态：{}", dept.getDeptName(), status);
    }
    
    /**
     * 构建树形结构
     */
    private List<DeptTreeVO> buildTree(List<SysDept> deptList, Long parentId) {
        List<DeptTreeVO> treeList = new ArrayList<>();
        
        for (SysDept dept : deptList) {
            if (parentId.equals(dept.getParentId())) {
                DeptTreeVO node = new DeptTreeVO();
                node.setId(dept.getId());
                node.setDeptName(dept.getDeptName());
                node.setParentId(dept.getParentId());
                node.setSort(dept.getOrderNum());
                node.setStatus(dept.getStatus());
                
                // 递归查找子节点
                List<DeptTreeVO> children = buildTree(deptList, dept.getId());
                if (!CollectionUtils.isEmpty(children)) {
                    node.setChildren(children);
                }
                
                treeList.add(node);
            }
        }
        
        return treeList;
    }
    
    /**
     * 更新祖级列表
     */
    @Transactional(rollbackFor = Exception.class)
    protected void updateAncestors(Long deptId, Long newParentId) {
        SysDept dept = getById(deptId);
        String oldAncestors = dept.getAncestors();
        
        // 设置新的祖级列表
        if (newParentId == null || newParentId == 0) {
            dept.setParentId(0L);
            dept.setAncestors("0");
        } else {
            SysDept parent = getById(newParentId);
            if (parent == null || parent.getDeleted() == 1) {
                throw new BusinessException("父部门不存在");
            }
            dept.setParentId(newParentId);
            dept.setAncestors(parent.getAncestors() + "," + parent.getId());
        }
        
        updateById(dept);
        
        // 更新所有子部门的祖级列表
        updateChildrenAncestors(deptId, oldAncestors, dept.getAncestors());
    }
    
    /**
     * 更新子部门的祖级列表
     */
    private void updateChildrenAncestors(Long parentId, String oldAncestors, String newAncestors) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_DEPT)
                .where(SYS_DEPT.PARENT_ID.eq(parentId))
                .and(SYS_DEPT.DELETED.eq(0));
        
        List<SysDept> children = list(wrapper);
        for (SysDept child : children) {
            String childOldAncestors = child.getAncestors();
            child.setAncestors(child.getAncestors().replace(oldAncestors, newAncestors));
            updateById(child);
            
            // 递归更新子部门
            updateChildrenAncestors(child.getId(), childOldAncestors, child.getAncestors());
        }
    }
    
    /**
     * 判断是否为子部门
     */
    private boolean isChildDept(Long parentId, Long childId) {
        SysDept child = getById(childId);
        if (child == null) {
            return false;
        }
        
        return StringUtils.hasText(child.getAncestors()) && 
               child.getAncestors().contains("," + parentId + ",");
    }
    
    /**
     * 转换为VO
     */
    private DeptVO convertToVO(SysDept dept) {
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);
        
        // 设置状态文本
        vo.setStatusText(dept.getStatus() == 1 ? "正常" : "禁用");
        
        // 查询父部门名称
        if (dept.getParentId() != null && dept.getParentId() != 0) {
            SysDept parent = getById(dept.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getDeptName());
            }
        }
        
        return vo;
    }
} 