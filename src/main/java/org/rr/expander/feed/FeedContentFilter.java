package org.rr.expander.feed;

import java.util.List;

import javax.annotation.Nonnull;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * The {@link FeedContentFilter} is responsible to filter the feed using regular expression
 * statements.
 */
public interface FeedContentFilter {

	/**
	 * Filter those feed entries away which match to the given regular expression. If the filter
	 * expression is not defined (null or empty) the given <code>feedEntries</code> will be returned.
	 * 
	 * @param feedEntries The feed entries which should be filtered.
	 * @return The filtered feed entries.
	 */
	public List<SyndEntry> filterInclude(@Nonnull List<SyndEntry> feedEntries);

	/**
	 * Filter those feed entries away which did not match to the given regular expression. If the
	 * filter expression is not defined (null or empty) the given <code>feedEntries</code> will be
	 * returned.
	 * 
	 * @param feedEntries The feed entries which should be filtered.
	 * @return The filtered feed entries.
	 */
	public List<SyndEntry> filterExclude(@Nonnull List<SyndEntry> feedEntries);

}
