package com.muxin.gateway.core.common;

import java.util.Collection;
import java.util.function.Predicate;


/**
 * 仓库接口
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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
