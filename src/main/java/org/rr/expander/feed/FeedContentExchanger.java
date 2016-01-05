package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.rr.expander.loader.UrlLoaderFactory;
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
	
	@Nonnull
	private final UrlLoaderFactory urlLoaderFactory;

	public FeedContentExchanger(@Nonnull String includeExpression, @Nonnull UrlLoaderFactory urlLoaderFactory) {
		this.includeExpression = StringUtils.defaultString(includeExpression);
		this.urlLoaderFactory = urlLoaderFactory;
	}

	/**
	 * Exchanges the content from each feed entry with the selected part of the linked web page.
	 * 
	 * @param feedEntries All entries which content should be exchanged.
	 */
	public void exchangeAll(@Nonnull List<SyndEntry> feedEntries) {
		ForkJoinPool executor = new ForkJoinPool(10);
		try {
			executor.submit(() ->
				feedEntries.parallelStream().forEach(feedEntry -> exchange(feedEntry))
			).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to fetch rss entries", e);
		} finally {
			executor.shutdown();
		}
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

	/**
	 * Applies the given html <code>pageContent</code> to the description or the first available
	 * content element of the given <code>feedEntry</code>.
	 * 
	 * @param feedEntry The feed entry where the <code>pageContent</code> should be applied to.
	 * @param pageContent The expanded html page which should be applied to the <code>feedEntry</code>.
	 */
	private void applyNewContentToEntry(@Nonnull SyndEntry feedEntry, @Nonnull String pageContent) {
		if(feedEntry.getDescription() != null) {
			applyNewContentToEntry(feedEntry.getDescription(), pageContent);
		} else if(feedEntry.getContents() != null) {
			applyNewContentToFirstEntry(feedEntry, pageContent);
		} else {
			logger.warn(String.format("Can not find any element in the feed entry %s to apply the page content", feedEntry));
		}
	}

	@SuppressWarnings("unchecked")
	private void applyNewContentToFirstEntry(@Nonnull SyndEntry feedEntry, @Nonnull String pageContent) {
		((Stream<SyndContent>) feedEntry.getContents().stream())
			.findFirst()
			.ifPresent(entry -> applyNewContentToEntry(entry, pageContent));
	}

	private void applyNewContentToEntry(@Nonnull SyndContent description, @Nonnull String pageContent) {
		description.setValue(pageContent);
		description.setType("html");
	}

	private @Nonnull String mergeHtmlElements(@Nonnull Collection<String> htmlElements) {
		return join(htmlElements, "\n");
	}

	private @Nonnull String loadPageContent(@Nonnull String link) throws IOException {
		return urlLoaderFactory.getUrlLoader(link).getContentAsString();
	}

}
