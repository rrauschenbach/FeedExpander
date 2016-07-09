package org.rr.expander.feed;

import javax.annotation.Nonnull;

/**
 * Factory interface which can be used with guice to create a {@link FeedContentExchanger} implementation.
 */
public interface FeedContentExchangerFactory {

	public FeedContentExchanger createFeedContentExchanger(@Nonnull String includeCssSelector);
	
}
