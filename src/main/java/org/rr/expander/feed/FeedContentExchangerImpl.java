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
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.rr.expander.cache.PageCache;
import org.rr.expander.loader.UrlLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

public class FeedContentExchangerImpl implements FeedContentExchanger {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(FeedContentExchangerImpl.class);

	@Nonnull
	private final String includeCssSelector;
	
	@Inject(optional = false)
	@Nonnull
	private UrlLoaderFactory urlLoaderFactory;
	
	@Inject(optional = false)
	@Nonnull
	private PageCache pageCache;
	
	@Inject
	public FeedContentExchangerImpl(
			@Assisted @Nonnull String includeCssSelector) {
		this.includeCssSelector = StringUtils.defaultString(includeCssSelector);
	}

	@Override
	public void exchangeAll(@Nonnull List<SyndEntry> feedEntries) {
		ForkJoinPool executor = new ForkJoinPool(10);
		try {
			executor.submit(() ->
				feedEntries.stream()
				.filter(feedEntry -> feedEntry != null)
				.forEach(feedEntry -> exchange(feedEntry))
			).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to fetch rss entries", e);
		} finally {
			executor.shutdown();
		}
	}

	private void exchange(@NotNull SyndEntry feedEntry) {
		try {
			String link = feedEntry.getLink();
			if (isNotBlank(link)) {
				String pageContent = loadPageContent(link);
				String extractedPageContent = mergeHtmlElements(new PageContentExtractor(includeCssSelector)
						.extractPageElements(pageContent, link));
				applyNewContentToEntry(feedEntry, extractedPageContent);
			}
		} catch (IOException | IllegalStateException e) {
			logger.warn(String.format("Failed to load link '%s'.", feedEntry.getLink()), e);
		}
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
		String pageContent;
		if((pageContent = pageCache.restore(link)) == null) {
			pageContent = pageCache.store(link, 
					urlLoaderFactory.getUrlLoader(link).getContentAsString());
		}
		return pageContent;
	}

}
