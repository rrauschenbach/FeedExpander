package org.rr.expander.cache;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PageCache} implementation which provides abstract methods which can be used to implement
 * the desired page compression and having a fixed maximum size. The least used pages will
 * automatically removed from the cache.
 */
abstract class CompressedMemPageCache implements PageCache {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(CompressedMemPageCache.class);
	
	private final Map<String, byte[]> cache;
	
	public CompressedMemPageCache(int maxSize) {
		if(maxSize <= 0) {
			throw new IllegalArgumentException(String.format("size %s is less than one.", maxSize));
		}
		this.cache = new LRUMap<>(maxSize);
	}

	@Override
	public String store(@Nonnull String url, @Nonnull String pageContent) {
		try {
			cache.put(createUrlKey(url), compress(pageContent));
		} catch (IOException e) {
			logger.error(String.format("Failed to store page content for '%s' to cache.", url), e);
		}
		return pageContent;
	}

	@Override
	public @Nullable String restore(@Nonnull String url) {
		try {
			byte[] compressed = cache.get(createUrlKey(url));
			if(compressed != null) {
				return decompress(compressed);
			}
		} catch (IOException e) {
			logger.error(String.format("Failed to restore page content for '%s' to cache.", url), e);
		}
		return null;
	}

	private @Nonnull String createUrlKey(@Nonnull String url) {
		return normalizeUrl(url);
	}
	
	private @Nonnull String normalizeUrl(@Nonnull String url) {
		try {
			URI normalizedUrl = URI.create(url).normalize();
			return new StringBuilder()
				.append(normalizedUrl.getHost())
				.append(":").append(normalizedUrl.getPort())
				.append("/").append(getPath(normalizedUrl))
				.append("&").append(normalizedUrl.getQuery())
				.toString();
		} catch(Exception e) {
			logger.warn(String.format("Failed to normalize url '%s'", url), e);
		}
		return url;
	}
	
	private @Nonnull String getPath(@Nonnull URI uri) {
		return Optional.of(uri)
			.map(url -> url.getPath())
			.map(path -> StringUtils.stripStart(path, "/"))
			.get();
	}
	
	protected abstract @Nonnull byte[] compress(@Nonnull String pageContent) throws IOException;
	
	protected abstract @Nonnull String decompress(@Nonnull byte[] compressed) throws IOException ;

}
