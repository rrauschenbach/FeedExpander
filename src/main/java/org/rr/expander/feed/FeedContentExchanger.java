package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.rr.expander.util.HttpInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * The {@link FeedContentExchanger} is responsible to load the linked web page content and place it at the feed entries.
 */
public class FeedContentExchanger {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(FeedContentExchanger.class);

	@Nonnull
	private final String includeExpression;

	public FeedContentExchanger(@Nonnull String includeExpression) {
		this.includeExpression = includeExpression;
	}

	/**
	 * Exchanges the content from each feed entry with the selected part of the linked web page.
	 * 
	 * @param feedEntries All entries which content should be exchanged.
	 */
	public void exchangeAll(@Nonnull List<SyndEntry> feedEntries) {
		feedEntries.stream().forEach(feedEntry -> exchange(feedEntry));
	}

	private @Nullable SyndEntry exchange(@Nullable SyndEntry feedEntry) {
		try {
			String link = feedEntry.getLink();
			if (isNotBlank(link)) {
				String pageContent = loadPageContent(link);
				String extractedPageContent = mergeHtmlElements(new PageContentExtractor(includeExpression)
						.extractPageElements(pageContent, link));
				applyNewContentToEntry(feedEntry, extractedPageContent);
			}
		} catch (IOException e) {
			logger.warn(String.format("Failed to load link '%s'.", feedEntry.getLink()), e);
		}
		return feedEntry;
	}

	private void applyNewContentToEntry(@Nonnull SyndEntry feedEntry, @Nonnull String pageContent) {
		SyndContent description = feedEntry.getDescription();
		description.setValue(pageContent);
		description.setType("html");
	}

	private @Nonnull String mergeHtmlElements(@Nonnull Collection<String> htmlElements) {
		return join(htmlElements, "\n");
	}

	private @Nonnull String loadPageContent(@Nonnull String link) throws IOException {
		return IOUtils.toString(new HttpInputStream(link));
	}

}
