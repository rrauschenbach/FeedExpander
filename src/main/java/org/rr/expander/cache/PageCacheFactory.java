package org.rr.expander.cache;

import javax.annotation.Nonnull;

import jersey.repackaged.com.google.common.base.Preconditions;

/**
 * A factory which is able to create specific implementation of {@link PageCache}.
 */
public abstract class PageCacheFactory {

	public static enum CACHE_TYPE {
		MEMCACHE_BZIP {
			@Override
			public PageCache createPageCache(int maxSize) {
				return new BZip2MemPageCacheImpl(maxSize);
			}
		};

		public abstract PageCache createPageCache(int maxSize);
	}

	public static final PageCacheFactory createPageCacheFactory(@Nonnull CACHE_TYPE type, int maxSize) {
		Preconditions.checkNotNull(type);
		return new PageCacheFactory() {

			@Override
			public PageCache getPageCache() {
				return type.createPageCache(maxSize);
			}
		};
	}

	public abstract PageCache getPageCache();

}
