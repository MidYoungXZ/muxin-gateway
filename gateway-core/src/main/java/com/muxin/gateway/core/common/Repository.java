package com.muxin.gateway.core.common;

import java.util.Collection;
import java.util.function.Predicate;


/**
 * 仓库接口
 *
 * @param <ID> 仓唯一标识
 * @param <T>  仓库实体
 */
public interface Repository<ID, T> {

    T save(T entity);

    void removeByUniqueCode(ID id);

    T findByUniqueCode(ID id);

    Collection<T> findAll();

    default Collection<T> findBy(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

}
