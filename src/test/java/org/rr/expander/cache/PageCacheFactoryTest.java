package org.rr.expander.cache;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.rr.expander.cache.PageCacheFactory.CACHE_TYPE;

public class PageCacheFactoryTest {

	@Test
	public void testCacheTypeAlwaysCreateInstances() {
		for (CACHE_TYPE pageCache : PageCacheFactory.CACHE_TYPE.values()) {
			// must never return null
			assertNotNull(pageCache.createPageCache(20, EMPTY));
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullType() {
		PageCacheFactory.createPageCacheFactory(null, 20, EMPTY);
	}
}
