package org.rr.expander.feed;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import org.rr.expander.loader.UrlLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * The {@link FeedBuilderImpl} can be used to handle the whole load, expand and new feed generation
 * process by simply invoking <code>loadFeed().expand(expression).build()</code>.
 */
public class FeedBuilderImpl implements FeedBuilder {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(FeedBuilderImpl.class);

	@Nonnull
	private static final String APPLICATION = "application";

	/** contains the http url to the feed that should be expanded. */
	@Nonnull
	private final String feedUrl;
	
	@Inject(optional = false)
	@Nonnull
	private UrlLoaderFactory urlLoaderFactory;

	/** The loaded rss or atom feed. */
	@Nullable
	private SyndFeed loadedFeed;
	
	@Inject(optional = false)
	@Nonnull
	private FeedContentExchangerFactory feedContentExchangerFactory;
	
	@Inject(optional = false)
	@Nonnull
	private FeedContentFilterFactory feedContentFilterFactory;


	@Inject
	public FeedBuilderImpl(
			@Assisted @Nullable String feedUrl) {
		if(feedUrl == null) {
			throw new IllegalArgumentException("The feed url must not be null.");
		}
		this.feedUrl = feedUrl;
	}

	@Override
	public @Nonnull FeedBuilderImpl loadFeed() throws MalformedURLException, FeedException, IOException {
		InputStream feedContentStream = urlLoaderFactory.getUrlLoader(feedUrl).getContentAsStream(StandardCharsets.UTF_8);
		loadedFeed = new SyndFeedInput().build(new XmlReader(feedContentStream));
		return this;
	}

	@Override
	public @Nonnull MediaType getMediaType() {
		return Optional.ofNullable(loadedFeed)
				.map(feed -> loadedFeed.getFeedType())
				.map(feedType -> {
					if (feedType.startsWith("rss")) {
						return new MediaType(APPLICATION, "rss+xml", UTF_8.name());
					} else if (feedType.startsWith("atom")) {
						return new MediaType(APPLICATION, "atom+xml", UTF_8.name());
					} else {
						return MediaType.APPLICATION_OCTET_STREAM_TYPE;
					}
				})
				.orElseThrow((() -> new IllegalArgumentException(String.format("Feed '%s' is not loaded.", feedUrl))));
	}

	@Override
	public @Nonnull FeedBuilderImpl expand(@Nullable String includeCssSelector) {
		if(isNotBlank(includeCssSelector)) {
			Optional.<String> of(includeCssSelector)
					.ifPresent(selector -> feedContentExchangerFactory.createFeedContentExchanger(selector).exchangeAll(getEntries()));
		}
		return this;
	}
	

	@Override
	public @Nonnull FeedBuilderImpl filter(@Nonnull List<String> includeFilter, @Nonnull List<String> excludeFilter) {
		includeFilter.stream().forEach(filter -> 
			applyFeedEntries(feedContentFilterFactory.createFeedContentFilter(filter).filterInclude(getEntries())));
		excludeFilter.stream().forEach(filter -> 
	  applyFeedEntries(feedContentFilterFactory.createFeedContentFilter(filter).filterExclude(getEntries())));
		return this;
	}

	@Override
	public @Nonnull byte[] build() throws FeedException {
		return Optional.ofNullable(loadedFeed)
			.map(feed -> buildFeed(feed))
			.orElseThrow(() -> (new IllegalArgumentException(
					String.format("Feed '%s' is not loaded or could not be generated.", feedUrl))));
	}

	@Override
	@Nonnull
	public FeedBuilderImpl applyLimit(@Nullable Integer limit) {
		reduceEntries(limit != null && limit >= 0 ? limit : Integer.MAX_VALUE);
		return this;
	}

	private @Nullable byte[] buildFeed(@Nonnull SyndFeed feed) {
		try {
			SyndFeedOutput output = new SyndFeedOutput();
			feed.setEncoding(UTF_8.name());
			return output.outputString(feed, true).getBytes(UTF_8);
		} catch (FeedException e) {
			logger.error(String.format("Failed to build feed '%s'", feedUrl), e);
		}
		return null;
	}

	/**
	 * Get all feed entries (for each article one) from the loaded feed.
	 * 
	 * @return All feed entries from the loaded feed.
	 * @throws IllegalArgumentException if the feed is not loaded before.
	 */
	@SuppressWarnings("unchecked")
	private @Nonnull List<SyndEntry> getEntries() {
		return Optional.ofNullable(loadedFeed)
				.map(feed -> feed.getEntries())
				.orElseThrow(() -> new IllegalArgumentException(String.format("Feed '%s' is not loaded.", feedUrl)));
	}

	/**
	 * Reduces the number of entries to the limit which was defined with {@link #applyLimit(Integer)}.
	 * 
	 * @return The reduces feed entry list.
	 * @throws IllegalArgumentException if the feed is not loaded before.
	 */
	private void reduceEntries(int limit) {
		Optional.ofNullable(loadedFeed)
			.map(feed -> getEntries().stream()
			.limit(limit)
			.collect(toList()))
			.map(entries -> applyFeedEntries(entries))
			.orElseThrow(() -> new IllegalArgumentException(String.format("Feed '%s' is not loaded.", feedUrl)));
	}

	/**
	 * Applies the given feed entries to the {@link #loadedFeed} instance which makes them public available. This is
	 * always necessary if entries gets added or removed or newly arranged.
	 * 
	 * @param entries The entries to be applied.
	 * @return The given feed entries.
	 */
	private @Nonnull List<SyndEntry> applyFeedEntries(@Nonnull List<SyndEntry> entries) {
		Optional.ofNullable(loadedFeed).ifPresent(feed -> feed.setEntries(entries));
		return entries;
	}
	
}
