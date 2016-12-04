package org.rr.expander.feed;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.rr.expander.feed.HtmlUtils.cleanHtml;
import static org.rr.expander.feed.HtmlUtils.stripHtml;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import org.rr.expander.loader.UrlLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * The {@link FeedBuilderImpl} can be used to handle the whole load, expand and new feed generation
 * process by simply invoking <code>loadFeed().expand(expression).build()</code>.
 */
public class FeedCreatorImpl implements FeedCreator {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(FeedCreatorImpl.class);

	@Nonnull
	private static final String APPLICATION = "application";
	
	@Nonnull
	private static final Pattern HREF_PATTERN = Pattern.compile("href=\"(.*?)\"");

	/** contains the http url to the feed that should be expanded. */
	@Nonnull
	private final String pageUrl;
	
	@Inject(optional = false)
	@Nonnull
	private UrlLoaderFactory urlLoaderFactory;

	/** The result atom feed. */
	@Nonnull
	private SyndFeed feed = new SyndFeedImpl();

	@Inject
	public FeedCreatorImpl(
			@Assisted @Nullable String pageUrl) {
		if(pageUrl == null) {
			throw new IllegalArgumentException("The page url must not be null.");
		}

		this.pageUrl = pageUrl;
	}
	
	@Override
	public @Nonnull FeedCreatorImpl createFeed(@Nullable String itemSelector, @Nullable String titleSelector,
			@Nullable String linkSelector, @Nullable String authorSelector)
			throws MalformedURLException, FeedException, IOException {
		if(itemSelector == null) {
			throw new IllegalArgumentException(String.format("The item selector for the page %s must not be null.", pageUrl));
		}
		String pageContent = urlLoaderFactory.getUrlLoader(pageUrl).getContentAsString();
		
		feed.setFeedType("atom_0.3");
		feed.setEntries(new PageContentExtractor(itemSelector)
			.extractPageElements(pageContent, pageUrl)
			.getGroupedPageElements()
			.stream()
			.map(extractedPageElement -> createFeedEntry(extractedPageElement, titleSelector, linkSelector, authorSelector))
			.collect(Collectors.toList()));
		
		return this;
	}

	private @Nonnull SyndEntry createFeedEntry(@Nonnull String extractPageElement, @Nullable String titleSelector,
			@Nullable String linkSelector, @Nullable String authorSelector) {
		SyndEntry entry = new SyndEntryImpl();
		if(titleSelector != null) {
			entry.setTitle(stripHtml(getText(extractPageElement, titleSelector)));
		}
		if(linkSelector != null) {
			entry.setLink(getHref(getText(extractPageElement, linkSelector)));
		}
		if(authorSelector != null) {
			entry.setAuthor(stripHtml(getText(extractPageElement, authorSelector)));
		}
		
		SyndContent description = new SyndContentImpl();
		description.setType("text/html");
		description.setValue(cleanHtml(extractPageElement));
		entry.setDescription(description);
		return entry;
	}

	private @Nonnull String getText(@Nonnull String extractPageElement, @Nonnull String selector) {
		return new PageContentExtractor(selector)
				.extractPageElements(extractPageElement, pageUrl)
				.getMergedPageElements();
	}
	
	private @Nonnull String getHref(@Nonnull String html) {
		Matcher matcher = HREF_PATTERN.matcher(html);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return EMPTY;
	}
	
	@Override
	public @Nonnull MediaType getMediaType() {
		return new MediaType(APPLICATION, "atom+xml", UTF_8.name());
	}

	@Override
	public @Nonnull byte[] build() throws FeedException {
		return Optional.ofNullable(feed)
			.map(feed -> buildFeed(feed))
			.orElseThrow(() -> (new IllegalArgumentException(
					String.format("Feed '%s' is not loaded or could not be generated.", pageUrl))));
	}

	private @Nullable byte[] buildFeed(@Nonnull SyndFeed feed) {
		try {
			SyndFeedOutput output = new SyndFeedOutput();
			feed.setEncoding(UTF_8.name());
			return output.outputString(feed, true).getBytes(UTF_8);
		} catch (FeedException e) {
			logger.error(String.format("Failed to build feed '%s'", pageUrl), e);
		}
		return null;
	}
	
}
