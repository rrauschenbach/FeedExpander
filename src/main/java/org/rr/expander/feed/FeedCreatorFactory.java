package org.rr.expander.feed;

import javax.annotation.Nullable;

/**
 * Factory interface which can be used with guice to create a {@link FeedCreator} implementation.
 */
public interface FeedCreatorFactory {
	
	public FeedCreator createFeedBuilder(@Nullable String feedUrl);
	
}
