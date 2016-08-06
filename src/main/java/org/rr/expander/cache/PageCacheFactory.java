package org.rr.expander.cache;

import javax.annotation.Nonnull;

import jersey.repackaged.com.google.common.base.Preconditions;

/**
 * A factory which is able to create specific implementation of {@link PageCache}.
 */
public abstract class PageCacheFactory {

	public static enum CACHE_TYPE {
		EH_CACHE {
			
			private EhCache singleton;
			
			@Override
			public PageCache createPageCache(String configurationFileName) {
				if(singleton == null) {
					singleton = new EhCache(configurationFileName);
				}
				return singleton;
			}
		};

		public abstract PageCache createPageCache(String configurationFileName);
	}

	public static final PageCacheFactory createPageCacheFactory(@Nonnull CACHE_TYPE type) {
		Preconditions.checkNotNull(type);
		return new PageCacheFactory() {

			@Override
			public PageCache getPageCache(String configurationFileName) {
				return type.createPageCache(configurationFileName);
			}
		};
	}

	@Nonnull
	public abstract PageCache getPageCache(String configurationFileName);

}
