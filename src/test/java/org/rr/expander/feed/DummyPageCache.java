package org.rr.expander.feed;

import org.rr.expander.cache.PageCache;

/**
 * {@link PageCache} implementation which never stores or restores any page content. 
 */
public class DummyPageCache implements PageCache {

	@Override
	public String store(String url, String pageContent) {
		return pageContent;
	}

	@Override
	public String restore(String url) {
		return null;
	}

	@Override
	public double getCacheHitCount() {
		return 0;
	}

	@Override
	public double getCacheMissCount() {
		return 0;
	}

}
