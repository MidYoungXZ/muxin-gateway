package com.muxin.gateway.core.common;

/**
 * 定义了一个通用的CRUD（创建、读取、更新、删除）仓库接口。
 * 该接口提供了基本的数据持久化操作方法，适用于各种实体对象的增删改查。
 *
 * @param <T> 实体对象类型
 * @param <ID> 实体对象的标识符类型
 * @author Administrator
 * @date 2025/1/10 09:45
 */
public interface CrudRepository<T, ID> {

    /**
     * 保存或更新一个实体对象。
     * 如果实体对象已存在，则更新；如果不存在，则插入新记录。
     *
     * @param entity 要保存或更新的实体对象
     * @return 保存或更新后的实体对象
     */
    T save(T entity);

    /**
     * 根据ID删除一个实体对象。
     *
     * @param id 实体对象的唯一标识符
     */
    void deleteById(ID id);

    /**
     * 根据ID查找一个实体对象。
     *
     * @param id 实体对象的唯一标识符
     * @return 查找到的实体对象，如果没有找到则返回null
     */
    T findById(ID id);

    /**
     * 查找所有实体对象。
     *
     * @return 所有实体对象的迭代器
     */
    Iterable<T> findAll();
}
