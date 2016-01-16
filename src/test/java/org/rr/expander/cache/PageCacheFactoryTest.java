package org.rr.expander.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.rr.expander.cache.PageCacheFactory.CACHE_TYPE;

public class PageCacheFactoryTest {

	@Test
	public void testCacheTypeAlwaysCreateInstances() {
		for (CACHE_TYPE pageCache : PageCacheFactory.CACHE_TYPE.values()) {
			// must never return null
			assertNotNull(pageCache.createPageCache(10));
			
			// must always create a new instance
			assertTrue(pageCache.createPageCache(10) != pageCache.createPageCache(10));
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullType() {
		PageCacheFactory.createPageCacheFactory(null, 10);
	}
}
