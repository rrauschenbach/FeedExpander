package org.rr.expander.feed;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.ObjectUtils;
import org.rr.expander.loader.UrlLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * The {@link FeedBuilder} can be used to handle the whole load, expand and new feed generation
 * process by simply invoking <code>loadFeed().expand(expression).build()</code>.
 */
public class FeedBuilder {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(FeedBuilder.class);

	@Nonnull
	private static final String APPLICATION = "application";

	/** contains the http url to the feed that should be expanded. */
	@Nonnull
	private final String feedUrl;
	
	@Nonnull
	private final UrlLoaderFactory urlLoaderFactory;
	
	/** max number of feed entries for the result feed */
	private int limit;

	/** The loaded rss or atom feed. */
	@Nullable
	private SyndFeed loadedFeed;

	public FeedBuilder(@Nonnull String feedUrl, @Nonnull UrlLoaderFactory urlLoaderFactory) {
		this.feedUrl = feedUrl;
		this.urlLoaderFactory = urlLoaderFactory;
		this.limit = Integer.MAX_VALUE;
	}

	/**
	 * Starts fetching the feed from the {@link #feedUrl} specified with the class constructor.
	 * 
	 * @return This {@link FeedBuilder} instance.
	 */
	public @Nonnull FeedBuilder loadFeed() throws MalformedURLException, FeedException, IOException {
		InputStream feedContentStream = urlLoaderFactory.getUrlLoader(feedUrl).getContentAsStream(StandardCharsets.UTF_8);
		loadedFeed = new SyndFeedInput().build(new XmlReader(feedContentStream));
		return this;
	}

	/**
	 * Get the mime type of the feed which is handled by this {@link FeedBuilder} instance. The method
	 * {@link #loadFeed()} must be invoked before this method can be used.
	 * 
	 * @return The mime of the feed.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
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

	/**
	 * Applies the given <code>includeExpression</code> to the linked page behind each feed entry of
	 * the loaded feed and attach the result to the result feed. The {@link #loadFeed()} method must
	 * be invoked before this method can be used. Otherwise no filter will be applied.
	 * 
	 * @param includeExpression The include expression which is used to filter the page content of the
	 *        linked web page.
	 * @return This {@link FeedBuilder} instance.
	 */
	public @Nonnull FeedBuilder expand(@Nullable String includeExpression) {
		Optional.<String> ofNullable(includeExpression)
				.ifPresent(expression -> new FeedContentExchanger(expression, urlLoaderFactory).exchangeAll(sortAndReduceEntries()));
		return this;
	}

	/**
	 * Create a new, utf-8 encoded feed from the current, modified loaded feed. The
	 * {@link #loadFeed()} method must be invoked first.
	 * 
	 * @return Get the XML representation for the current feed state.
	 * @throws FeedException thrown if the XML representation for the feed could not be created.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
	public @Nonnull byte[] build() throws FeedException {
		return Optional.ofNullable(loadedFeed)
			.map(feed -> buildFeed(feed))
			.orElseThrow(() -> (new IllegalArgumentException(
					String.format("Feed '%s' is not loaded or could not be generated.", feedUrl))));
	}

	/**
	 * Sets the number of entries in the result feed. All entries behind this limit will be removed
	 * from the result feed and will not be expanded.
	 * 
	 * @param limit Number of feed entries kept for the result feed.
	 */
	@Nonnull
	public FeedBuilder setLimit(@Nullable Integer limit) {
		this.limit = limit != null && limit >= 0 ? limit : Integer.MAX_VALUE;
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
	 * First sorts the loaded feed entries by published date and than reduces the number of entries to
	 * the limit which was defined with {@link #setLimit(Integer)}.
	 * 
	 * @return The sorted and reduces feed entry list.
	 * @throws IllegalArgumentException if the feed is not loaded before.
	 */
	private @Nonnull List<SyndEntry> sortAndReduceEntries() {
		return Optional.ofNullable(loadedFeed)
			.map(feed -> getEntries().stream()
			.sorted((d1, d2) ->  ObjectUtils.compare(d1.getPublishedDate(), d2.getPublishedDate()))
			.limit(limit)
			.collect(toList()))
			.map(entries -> applyFeedEntries(entries))
			.orElseThrow(() -> new IllegalArgumentException(String.format("Feed '%s' is not loaded.", feedUrl)));
	}

	private @Nonnull List<SyndEntry> applyFeedEntries(@Nonnull List<SyndEntry> entries) {
		Optional.ofNullable(loadedFeed).ifPresent(feed -> feed.setEntries(entries));
		return entries;
	}
	
}
