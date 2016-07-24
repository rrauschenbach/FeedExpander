package org.rr.expander.feed;

import javax.annotation.Nonnull;

/**
 * Factory interface which can be used with guice to create a {@link FeedContentFilter} implementation.
 */
public interface FeedContentFilterFactory {

	public FeedContentFilter createFeedContentFilter(@Nonnull String regex);
	
}
