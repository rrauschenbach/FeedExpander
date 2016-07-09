package org.rr.expander.cache;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

public class DiskPageCache implements PageCache {
	
	private static final String DEFAULT_CACHE_FILE_LOCATION = System.getProperty("java.io.tmpdir") + File.separator + "feed-cache";
	
	private String location;
	
	private long maxSize;
	
	private Cache<String, String> cache;
	
	public DiskPageCache(long maxSize, String location) {
		this.maxSize = maxSize;
		this.location = location;
		
		init();
	}
	
	private void init() {
		PersistentCacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
		    .with(CacheManagerBuilder.persistence(getStoragePath())) 
		    .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
		        ResourcePoolsBuilder.newResourcePoolsBuilder()
		            .heap(10, MemoryUnit.MB)
		            .disk(maxSize, MemoryUnit.MB, true)) 
		        )
		    .build(true);
		
		cache = cacheManager.getCache("persistent-cache", String.class, String.class);
	}
	
	private String getStoragePath() {
		return StringUtils.defaultIfBlank(location, DEFAULT_CACHE_FILE_LOCATION);
	}

	@Override
	public String store(String url, String pageContent) {
		cache.put(url, pageContent);
		return pageContent;
	}

	@Override
	public String restore(String url) {
		return cache.get(url);
	}

}
