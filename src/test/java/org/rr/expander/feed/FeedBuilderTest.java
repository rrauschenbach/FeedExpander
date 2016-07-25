package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.junit.Test;
import org.rr.expander.cache.PageCache;
import org.rr.expander.loader.UrlLoaderFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import junit.framework.AssertionFailedError;

public class FeedBuilderTest {

	private static final String CONTENT_CSS_SELECTOR = "div#main";

	@Test
	public void testBuildSuccess() throws Exception {
		String expandedFeed = new String(createFeedBuilder("feed_1.xml").loadFeed().expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		String extractedPageContent1 = stripHtml(extractPageContent(getHtmlPageContent("content_1.html"), CONTENT_CSS_SELECTOR));
		String extractedPageContent2 = stripHtml(extractPageContent(getHtmlPageContent("content_2.html"), CONTENT_CSS_SELECTOR));
		
		// the feed contains two entries.
		assertEquals(2, createSyndFeed(expandedFeed).getEntries().size());
		
		assertTrue(contains(expandedFeed, extractedPageContent1));
		assertTrue(contains(expandedFeed, extractedPageContent2));
	}
	
	@Test
	public void testBuildSuccessWithLimit() throws Exception {
		String expandedFeed = new String(createFeedBuilder("feed_1.xml").loadFeed().applyLimit(1).expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		
		// the reduced feed contains one entry.
		assertEquals(1, createSyndFeed(expandedFeed).getEntries().size());

		// test that the first entry is returned.
		String extractedPageContent2 = stripHtml(extractPageContent(getHtmlPageContent("content_1.html"), CONTENT_CSS_SELECTOR));
		assertTrue(contains(expandedFeed, extractedPageContent2));
	}

	@Test(expected=IOException.class)
	public void testBuildFailedNonExistingFeed() throws Exception {
		new String(createFeedBuilder("not_existing.xml").loadFeed().expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void testBuildFailedInvalidFeed() throws Exception {
		String feed = new String(createFeedBuilder("invalid_feed.xml").loadFeed().build(), StandardCharsets.UTF_8);
		assertTrue(contains(feed, "<feed xmlns=\"http://www.w3.org/2005/Atom\" />")); // empty feed
	}

	@Test
	public void testBuildFailedInvalidFeedLinks() throws Exception {
		String feed = new String(createFeedBuilder("invalid_feed_links.xml").loadFeed().expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		String extractedResultFeedContent = stripHtml(extractPageContent(feed, "summary"));
		String extractedOriginalFeedContent = stripHtml(extractPageContent(getHtmlPageContent("invalid_feed_links.xml"), "summary"));

		// the feed entries must leave untouched if the links can't be loaded.
		assertEquals(extractedResultFeedContent, extractedOriginalFeedContent);
	}
	
	@Test
	public void testBuildFailedInvalidNonMatchingPageSelector() throws Exception {
		String feed = new String(createFeedBuilder("feed_1.xml").loadFeed().expand("#unknown").build(), StandardCharsets.UTF_8);
		String extractedFeedContent = stripHtml(extractPageContent(feed, "summary"));
		
		// the feed entries must be empty if the selector did not match somewhere in the linked page.
		assertEquals(EMPTY, extractedFeedContent);
	}
	
	@Test(expected=IOException.class)
	public void testBuildFailedFeedDidNotExists() throws Exception {
		createFeedBuilder("not_existing.xml").loadFeed();
	}
	
	private SyndFeed createSyndFeed(String expandedFeed) throws FeedException, IOException {
		SyndFeed feed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(expandedFeed.getBytes())));
		return feed;
	}

	private String getHtmlPageContent(String page) throws IOException {
		return createUrlLoaderFactory().getUrlLoader("test://" + page).getContentAsString();
	}

	private String extractPageContent(String content1, String includeCssSelector) {
		List<String> extractPageElements = new PageContentExtractor(includeCssSelector).extractPageElements(content1, "http://test.de");
		if(!extractPageElements.isEmpty()) {
			return extractPageElements.get(0);
		}
		throw new AssertionFailedError(String.format("Failed to load page %s and extract %s", content1, includeCssSelector));
	}
	
	private String stripHtml(String html) {
		return Jsoup.parse(html).text();
	}
	
	private FeedBuilder createFeedBuilder(String feed) {
		return createInjector().getInstance(FeedBuilderFactory.class).createFeedBuilder("test://" + feed);
	}

	private DummyPageCache createPageCache() {
		return new DummyPageCache();
	}

	private TestUrlLoaderFactory createUrlLoaderFactory() {
		return new TestUrlLoaderFactory();
	}
	
	private Injector createInjector() {
    return Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
        	bindUrlLoaderFactory();
        	bindPageCache();
        	bindFeedBuilder();
        	bindFeedContentExchanger();
        	bindFeedContentFilter();

        }

				private void bindUrlLoaderFactory() {
					bind(UrlLoaderFactory.class).toInstance(createUrlLoaderFactory());
				}

				private void bindPageCache() {
					bind(PageCache.class).toInstance(createPageCache());
				}

				private void bindFeedBuilder() {
					install(new FactoryModuleBuilder()
        	     .implement(FeedBuilder.class, FeedBuilderImpl.class)
        	     .build(FeedBuilderFactory.class));
				}

				private void bindFeedContentExchanger() {
					install(new FactoryModuleBuilder()
       	     .implement(FeedContentExchanger.class, FeedContentExchangerImpl.class)
       	     .build(FeedContentExchangerFactory.class));
				}

				private void bindFeedContentFilter() {
					install(new FactoryModuleBuilder()
        	     .implement(FeedContentFilter.class, FeedContentFilterImpl.class)
        	     .build(FeedContentFilterFactory.class));
				}
    });
	}
	
}
