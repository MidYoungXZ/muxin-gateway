

package com.muxin.gateway.core.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 刷新路由事件
 */
public class RefreshRoutesEvent extends ApplicationEvent {

	private final Map<String, Object> metadata;

	/**
	 * Create a new ApplicationEvent.
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public RefreshRoutesEvent(Object source) {
		this(source, null);
	}

	/**
	 * Create a new ApplicationEvent that should refresh filtering by {@link #metadata}.
	 * @param source the object on which the event initially occurred (never {@code null})
	 * @param metadata map of metadata the routes should match ({code null} is considered
	 * a global refresh)
	 */
	public RefreshRoutesEvent(Object source, Map<String, Object> metadata) {
		super(source);
		this.metadata = metadata;
	}

	public boolean isScoped() {
		return !CollectionUtils.isEmpty(getMetadata());
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

}
