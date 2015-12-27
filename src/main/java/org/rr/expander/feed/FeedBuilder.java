package org.rr.expander.feed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.Charsets;
import org.rr.expander.util.HttpLoader;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * Statefull class which provides all feed functionality needed to load, expand and generate a new,
 * expanded feed.
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
	 * Starts fetching the feed from the {@link #feedUrl}.
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
	 * @throws IllegalArgumentException if the feed is not loaded before fetching its entries.
	 */
	public @Nonnull String getMimeType() {
		if (loadedFeed != null) {
			String feedType = loadedFeed.getFeedType();
			if (feedType.startsWith("rss")) {
				return "application/rss+xml";
			} else if (feedType.startsWith("atom")) {
				return "application/atom+xml";
			} else {
				return "application/octet-stream";
			}
		}
		throw new IllegalArgumentException("feed is not loaded.");
	}

	/**
	 * Applies the given <code>includeExpression</code> to the linked page behind each feed entry of
	 * the loaded feed and attach the result to the result feed. The method {@link #loadFeed()} must
	 * be invoked before this method can be used. Otherwise no filter will be applied.
	 * 
	 * @param includeExpression The include expression which is used to filter the page content of the
	 *        linked web page.
	 * @return This {@link FeedBuilder} instance.
	 * @throws IOException
	 */
	public @Nonnull FeedBuilder expand(@Nullable String includeExpression) {
		Optional.<String> ofNullable(includeExpression)
				.ifPresent(expression -> new FeedContentExchanger(expression).exchangeAll(getEntries()));
		return this;
	}

	/**
	 * Create a new xml from the current (possibly modified) state of the loaded feed.
	 * 
	 * @return Get the xml representation for the current feed state.
	 * @throws IOException
	 * @throws FeedException
	 */
	public @Nonnull byte[] build() throws IOException, FeedException {
		SyndFeedOutput output = new SyndFeedOutput();
		return output.outputString(loadedFeed, true).getBytes();
	}

	/**
	 * Get all feed entries (for each article one) from the loaded feed.
	 * 
	 * @return All feed entries from the loaded feed.
	 * @throws IllegalArgumentException if the feed is not loaded before fetching its entries.
	 */
	@SuppressWarnings("unchecked")
	private @Nonnull List<SyndEntry> getEntries() {
		if (loadedFeed != null) {
			return loadedFeed.getEntries();
		}
		throw new IllegalArgumentException("feed is not loaded.");
	}

}
