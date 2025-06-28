package com.muxin.gateway.core.plus.predicate;

import com.muxin.gateway.core.plus.LifeCycle;
import com.muxin.gateway.core.plus.Repository;
import com.muxin.gateway.core.plus.monitor.Monitorable;

/**
 * @author Administrator
 * @since 1.0
 */
public interface PredicateManager extends Repository<String, Predicate>, Monitorable, LifeCycle {


}
