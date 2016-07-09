package org.rr.expander.feed;

import java.util.List;

import javax.annotation.Nonnull;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * The {@link FeedContentExchanger} is responsible to load the linked web page content and place it at the feed entries.
 */
public interface FeedContentExchanger {

	/**
	 * Exchanges the content from each feed entry with the selected part of the linked web page.
	 * 
	 * @param feedEntries All entries which content should be exchanged.
	 */
	public void exchangeAll(@Nonnull List<SyndEntry> feedEntries);
	
}
