package org.rr.expander.loader;

import javax.annotation.Nonnull;

/**
 * A factory which allows to create specific {@link UrlLoader} instances for different types of URL.
 */
public abstract class UrlLoaderFactory {
	
	/**
	 * Get a {@link UrlLoaderFactory} instance which creates {@link UrlLoader} instances.
	 * @return The desired {@link UrlLoader} instance.
	 */
	public static @Nonnull UrlLoaderFactory createURLLoaderFactory() {
		return new UrlLoaderFactory() {
			@Override
			public @Nonnull UrlLoader getUrlLoader(@Nonnull String url) {
				if(url.matches("http(s)?://.*")) {
					return new HttpUrlLoader(url);
				}
				throw new IllegalArgumentException(String.format("No UrlLoader instance for the url %s available.", url));
			}
		};
	}
	
	/**
	 * Get the {@link UrlLoader} implementation.
	 * 
	 * @param url The url to be used from the result {@link UrlLoader} instance.
	 * @return The desired {@link UrlLoader} instance.
	 * @throws IllegalArgumentException if the given url did not match to the desired {@link UrlLoader}.
	 */
	public abstract @Nonnull UrlLoader getUrlLoader(@Nonnull String url);
}
