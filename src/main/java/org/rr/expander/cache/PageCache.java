package org.rr.expander.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Specifies common methods for page caching.
 */
public interface PageCache {

	/**
	 * Store the given <code>pageContent</code> with the given <code>url</code> as key.
	 * 
	 * @param url The url string which is used as key. The same string must be used with the
	 *        {@link #restore(String)} method to access the stored <code>pageContent</code> with the
	 *        {@link #restore(String)} method.
	 * @param pageContent The page content / html page which should be stored.
	 * @return The stored pageContent given with the <code>pageContent</code> parameter.
	 */
	String store(@Nonnull String url, @Nonnull String pageContent);

	/**
	 * Gets the page content which was previously stored usind the given <code>url</code> as key.
	 * 
	 * @param url The url string which is used as key to access the page content.
	 * @return The desired page content or <code>null</code> if no matching page content exists.
	 */
	@Nullable
	String restore(@Nonnull String url);
}
