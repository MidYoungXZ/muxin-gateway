package com.muxin.gateway.core.plus.common;

import java.util.Collection;
import java.util.function.Predicate;


/**
 * 仓库接口
 *
 * @param <ID> 仓唯一标识
 * @param <T>  仓库实体
 */
public interface Repository<ID, T> {

    T insert(T entity);

    void deleteById(ID id);

    T selectById(ID id);

    Collection<T> selectAll();

    default Collection<T> selectBy(Predicate<T> predicate) {
        return selectAll().stream().filter(predicate).toList();
    }

}
