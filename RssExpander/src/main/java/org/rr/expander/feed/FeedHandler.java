package org.rr.expander.feed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.rr.expander.util.HttpInputStream;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * Statefull feed handler which is able to load a feed from a given url and
 * apply filter to its entries.
 */
public class FeedHandler {

	@Nullable
	private String feedUrl;

	@Nullable
	private SyndFeed loadedFeed;

	/**
	 * Specifies the url to a rss or atom feed which should be loaded with the
	 * {@link #loadFeed()} method.
	 * 
	 * @param feedUrl
	 *          The feed url to be loaded.
	 * @return This {@link FeedHandler} instance.
	 */
	public @Nonnull FeedHandler setFeedUrl(@Nonnull String feedUrl) {
		this.feedUrl = feedUrl;
		return this;
	}

	/**
	 * Starts fetching the feed from the {@link #feedUrl}.
	 * 
	 * @return This {@link FeedHandler} instance.
	 * @see #setFeedUrl(String)
	 */
	public @Nonnull FeedHandler loadFeed()
			throws IllegalArgumentException, MalformedURLException, FeedException, IOException {
		SyndFeedInput input = new SyndFeedInput();
		if (feedUrl != null) {
			loadedFeed = input.build(new XmlReader(new HttpInputStream(feedUrl)));
			return this;
		}
		throw new IllegalArgumentException("feed url is null.");
	}

	/**
	 * Get the mime type of the feed which is handles with this
	 * {@link FeedHandler}. The method {@link #loadFeed()} must be invoked before
	 * this method can be used.
	 * 
	 * @return The mime of the feed.
	 * @throws IllegalArgumentException
	 *           if the feed is not loaded before fetching its entries.
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
	 * Applies the given <code>includeExpression</code> to the feed entries of the
	 * loaded feed. The method {@link #loadFeed()} must be invoked before this
	 * method can be used. Otherwise no filter will be applied.
	 * 
	 * @param includeExpression
	 *          The include expression which is used to filter the page content of
	 *          the linked web page.
	 * @return This {@link FeedHandler} instance.
	 * @throws IOException
	 */
	public @Nonnull FeedHandler filterContent(@Nullable String includeExpression) throws IOException {
		if (includeExpression != null) {
			ContentFilter filter = new ContentFilter(includeExpression);
			filter.apply(getEntries());
		}
		return this;
	}

	/**
	 * Create a new xml from the current (possibly modified) state of the loaded
	 * feed.
	 * 
	 * @return Get the xml representation for the current feed state.
	 * @throws IOException
	 * @throws FeedException
	 */
	public @Nonnull byte[] toXml() throws IOException, FeedException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SyndFeedOutput output = new SyndFeedOutput();
		output.output(loadedFeed, new PrintWriter(out), true);
		return out.toByteArray();
	}

	/**
	 * Get all feed entries (for each article one) from the loaded feed.
	 * 
	 * @return All feed entries from the loaded feed.
	 * @throws IllegalArgumentException
	 *           if the feed is not loaded before fetching its entries.
	 */
	@SuppressWarnings("unchecked")
	private @Nonnull List<SyndEntry> getEntries() {
		if (loadedFeed != null) {
			return (List<SyndEntry>) loadedFeed.getEntries();
		}
		throw new IllegalArgumentException("feed is not loaded.");
	}

}
