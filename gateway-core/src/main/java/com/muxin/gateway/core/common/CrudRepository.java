package com.muxin.gateway.core.common;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 09:45
 */
public interface CrudRepository <T,ID>{

    T save(T entity);

    void deleteById(ID id);

    T findById(ID id);

    Iterable<T> findAll();
    
}
