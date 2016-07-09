package org.rr.expander.cache;

import javax.annotation.Nonnull;

import jersey.repackaged.com.google.common.base.Preconditions;

/**
 * A factory which is able to create specific implementation of {@link PageCache}.
 */
public abstract class PageCacheFactory {

	public static enum CACHE_TYPE {
		DISK_CACHE {
			
			private DiskPageCache singleton;
			
			@Override
			public PageCache createPageCache(long maxSize, String location) {
				if(singleton == null) {
					singleton = new DiskPageCache(maxSize, location);
				}
				return singleton;
			}
		};

		public abstract PageCache createPageCache(long maxSize, String location);
	}

	public static final PageCacheFactory createPageCacheFactory(@Nonnull CACHE_TYPE type, long maxSize, String location) {
		Preconditions.checkNotNull(type);
		return new PageCacheFactory() {

			@Override
			public PageCache getPageCache() {
				return type.createPageCache(maxSize, location);
			}
		};
	}

	@Nonnull
	public abstract PageCache getPageCache();

}
