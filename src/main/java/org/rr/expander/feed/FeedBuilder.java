package org.rr.expander.feed;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import org.rr.expander.util.HttpLoader;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * The {@link FeedBuilder} can be used to handle the whole load, expand and new feed generation process
 * by simply invoking <code>loadFeed().expand(expression).build()</code>.
 */
public class FeedBuilder {

	/** contains the http url to the feed that should be expanded. */
	@Nonnull
	private final String feedUrl;

	/** The loaded rss or atom feed. */
	@Nullable
	private SyndFeed loadedFeed;
	
	public FeedBuilder(@Nonnull String feedUrl) {
		this.feedUrl = feedUrl;
	}

	/**
	 * Starts fetching the feed from the {@link #feedUrl} specified with the class constructor.
	 * 
	 * @return This {@link FeedBuilder} instance.
	 */
	public @Nonnull FeedBuilder loadFeed() throws MalformedURLException, FeedException, IOException {
		loadedFeed = new SyndFeedInput().build(new XmlReader(new HttpLoader(feedUrl).getContentAsStream()));
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
		MediaType mediaType = Optional.ofNullable(loadedFeed).map(feed -> {
			String feedType = feed.getFeedType();
			if (feedType.startsWith("rss")) {
				return new MediaType("application", "rss+xml", UTF_8.name());
			} else if (feedType.startsWith("atom")) {
				return new MediaType("application", "atom+xml", UTF_8.name());
			} else {
				return new MediaType("application", "octet-stream", UTF_8.name());
			}
		}).get();
		
		if(mediaType != null) {
			return mediaType;
		}
		throw new IllegalArgumentException("feed is not loaded.");
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
				.ifPresent(expression -> new FeedContentExchanger(expression).exchangeAll(getEntries()));
		return this;
	}

	/**
	 * Create a new, utf-8 encoded feed from the current, modified loaded feed. The {@link #loadFeed()} method must
	 * be invoked first.
	 * 
	 * @return Get the XML representation for the current feed state.
	 * @throws FeedException thrown if the XML representation for the feed could not be created.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
	public @Nonnull byte[] build() throws FeedException {
		if(loadedFeed != null) {
			SyndFeedOutput output = new SyndFeedOutput();
			loadedFeed.setEncoding(UTF_8.name());
			return output.outputString(loadedFeed, true).getBytes(UTF_8);
		}
		throw new IllegalArgumentException("feed is not loaded.");
	}

	/**
	 * Get all feed entries (for each article one) from the loaded feed.
	 * 
	 * @return All feed entries from the loaded feed.
	 * @throws IllegalArgumentException if the feed is not loaded before.
	 */
	@SuppressWarnings("unchecked")
	private @Nonnull List<SyndEntry> getEntries() {
		if (loadedFeed != null) {
			return loadedFeed.getEntries();
		}
		throw new IllegalArgumentException("feed is not loaded.");
	}

}
