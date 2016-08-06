package org.rr.expander.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * {@link PageCache} implementation which uses the ehcache framework for caching.
 */
public class EhCache implements PageCache {

	private String configurationFileName;
	
	private Cache cache;
	
	public EhCache(String configurationFileName) {
		this.configurationFileName = configurationFileName;
		init();
	}
	
	private void init() {
		CacheManager cacheManager = CacheManager.create(configurationFileName);
		cache = cacheManager.getCache("pageContentCache");
	}

	@Override
	public String store(String url, String pageContent) {
		cache.put(new Element(url, pageContent));
		return pageContent;
	}

	@Override
	public String restore(String url) {
		Element element = cache.get(url);
		return element != null ? (String) element.getObjectValue() : null;
	}
	
	@Override
	public double getCacheHitCount() {
		return cache.getStatistics().cacheHitCount();
	}
	
	@Override
	public double getCacheMissCount() {
		return cache.getStatistics().cacheMissCount();
	}

}
