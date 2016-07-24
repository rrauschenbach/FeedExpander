package org.rr.expander.feed;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import com.sun.syndication.io.FeedException;

public interface FeedBuilder {

	/**
	 * Starts fetching the feed from the {@link #feedUrl} specified with the class constructor.
	 * 
	 * @return This {@link FeedBuilderImpl} instance.
	 */
	public @Nonnull FeedBuilderImpl loadFeed() throws MalformedURLException, FeedException, IOException;

	/**
	 * Get the mime type of the feed which is handled by this {@link FeedBuilderImpl} instance. The method
	 * {@link #loadFeed()} must be invoked before this method can be used.
	 * 
	 * @return The mime of the feed.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
	public @Nonnull MediaType getMediaType();

	/**
	 * Applies the given <code>includeCssSelector</code> to the linked page behind each feed entry of
	 * the loaded feed and attach the result to the result feed. The {@link #loadFeed()} method must
	 * be invoked before this method can be used. Otherwise no filter will be applied.
	 * 
	 * @param includeCssSelector The include expression which is used to filter the page content of the
	 *        linked web page.
	 * @return This {@link FeedBuilderImpl} instance.
	 */
	public @Nonnull FeedBuilderImpl expand(@Nullable String includeCssSelector);

	/**
	 * Filter feed entries using regular expressions.
	 * 
	 * @param includeFilter Filter away all feed entries matching to this regular expression.
	 * @param excludeFilter Filter away all feed entries not matching to this regular expression.
	 * @return This {@link FeedBuilderImpl} instance.
	 */
	FeedBuilderImpl filter(String includeFilter, String excludeFilter);

	/**
	 * Create a new, utf-8 encoded feed from the current, modified loaded feed. The
	 * {@link #loadFeed()} method must be invoked first.
	 * 
	 * @return Get the XML representation for the current feed state.
	 * @throws FeedException thrown if the XML representation for the feed could not be created.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
	public @Nonnull byte[] build() throws FeedException;

	/**
	 * Sets the number of entries in the result feed. All entries behind this limit will be removed
	 * from the result feed and will not be expanded.
	 * 
	 * @param limit Number of feed entries kept for the result feed.
	 */
	@Nonnull
	public FeedBuilderImpl applyLimit(@Nullable Integer limit);

}
