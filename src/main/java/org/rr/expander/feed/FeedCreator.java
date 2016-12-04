package org.rr.expander.feed;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import com.sun.syndication.io.FeedException;

public interface FeedCreator {
	
	/**
	 * Starts fetching the page from the {@link #pageUrl} specified with the class constructor.
	 * 
	 * @param itemSelector A css select statement which describes the location of the items of a web
	 *        page which should be the items of the feed.
	 * @param titleSelector A css select statement which describes the location of the title element
	 *        relative to the item which was selected with the itemSelector.
	 * @param linkSelector A css select statement which describes the location of the link element
	 *        relative to the item which was selected with the itemSelector.
	 * @return This {@link FeedCreator} instance.
	 */
	public @Nonnull FeedCreator createFeed(@Nullable String itemSelector, @Nullable String titleSelector, @Nullable String linkSelector, @Nullable String authorSelector) throws MalformedURLException, FeedException, IOException;

	/**
	 * Applies a title to the feed.
	 * 
	 * @param title The title for the feed.
	 * @return This {@link FeedCreator} instance.
	 */
	public @Nonnull FeedCreator setTitle(@Nullable String title); 
	
	/**
	 * Get the mime type of the feed which is handled by this {@link FeedBuilderImpl} instance. The method
	 * {@link #createFeed()} must be invoked before this method can be used.
	 * 
	 * @return The mime of the feed.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
	public @Nonnull MediaType getMediaType();
	
	/**
	 * Create a new, utf-8 encoded feed from the current, modified loaded feed. The
	 * {@link #createFeed()} method must be invoked first.
	 * 
	 * @return Get the XML representation for the current feed state.
	 * @throws FeedException thrown if the XML representation for the feed could not be created.
	 * @throws IllegalArgumentException if the feed was not loaded before.
	 */
	public @Nonnull byte[] build() throws FeedException;

}
