package org.rr.expander.health;

import javax.annotation.Nullable;

import org.rr.expander.cache.PageCache;

import com.codahale.metrics.health.HealthCheck;

public class PageCacheHealthCheck extends HealthCheck {

	@Nullable
	PageCache pageCache;
	
	public PageCacheHealthCheck(@Nullable PageCache pageCache) {
		this.pageCache = pageCache;
	}

	@Override
	protected Result check() throws Exception {
		if(pageCache != null) {
			double cacheHitCount = pageCache.getCacheHitCount();
			double cacheMissCount = pageCache.getCacheMissCount();
			return Result.healthy(String.format("Cache hits %s and misses %s times.", cacheHitCount, cacheMissCount));
		}
		return Result.unhealthy("No page cache available.");
	}
}
