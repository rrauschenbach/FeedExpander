package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		String expandedFeed = new String(createFeedBuilder("feeds/valid_feed/feed.xml").loadFeed().expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		String extractedPageContent1 = stripHtml(extractPageContent(getHtmlPageContent("feeds/valid_feed/content_1.html"), CONTENT_CSS_SELECTOR));
		String extractedPageContent2 = stripHtml(extractPageContent(getHtmlPageContent("feeds/valid_feed/content_2.html"), CONTENT_CSS_SELECTOR));
		
		// the feed contains two entries.
		assertEquals(2, createSyndFeed(expandedFeed).getEntries().size());
		
		// take sure that the context was exchanged and test for the original feed content.
		assertFalse(contains(expandedFeed, "Lorem ipsum"));
		
		assertTrue(contains(expandedFeed, extractedPageContent1));
		assertTrue(contains(expandedFeed, extractedPageContent2));
	}
	
	/**
	 * Test for illegal xml content which must not cause any parse or feed creation exception. Illegal
	 * characters must be escaped or removed by the feed expander.
	 */
	@Test
	public void testBuildSuccessWithIllegalPageContent() throws Exception {
		String expandedFeed = new String(createFeedBuilder("feeds/illegal_page_content/feed.xml").loadFeed().expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		
		// the feed contains two entries.
		assertEquals(2, createSyndFeed(expandedFeed).getEntries().size());
		
		// take sure that the context was exchanged and test for the original feed content.
		assertFalse(contains(expandedFeed, "Lorem ipsum"));
	}
	
	/**
	 * Test that the feed entry limit is taken under account. 
	 */
	@Test
	public void testBuildSuccessWithLimit() throws Exception {
		String expandedFeed = new String(createFeedBuilder("feeds/valid_feed/feed.xml").loadFeed().applyLimit(1).expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		
		// the reduced feed contains one entry.
		assertEquals(1, createSyndFeed(expandedFeed).getEntries().size());

		// test that the first entry is returned.
		String extractedPageContent2 = stripHtml(extractPageContent(getHtmlPageContent("feeds/valid_feed/content_1.html"), CONTENT_CSS_SELECTOR));
		assertTrue(contains(expandedFeed, extractedPageContent2));
	}

	/**
	 * Test for unexpected xml instead of some valid feed data. This will cause an empty feed.
	 */
	@Test
	public void testBuildFailedInvalidFeedXmlContent() throws Exception {
		String feed = new String(createFeedBuilder("feeds/invalid_xml_content/feed.xml").loadFeed().build(), StandardCharsets.UTF_8);
		assertTrue(contains(feed, "<feed xmlns=\"http://www.w3.org/2005/Atom\" />")); // empty feed
	}

	/**
	 * Test for dead links which should cause that the feed entry stays untouched.
	 */
	@Test
	public void testBuildFailedInvalidFeedLinks() throws Exception {
		String feed = new String(createFeedBuilder("feeds/invalid_links_feed/feed.xml").loadFeed().expand(CONTENT_CSS_SELECTOR).build(), StandardCharsets.UTF_8);
		String extractedResultFeedContent = stripHtml(extractPageContent(feed, "summary"));
		String extractedOriginalFeedContent = stripHtml(extractPageContent(getHtmlPageContent("feeds/invalid_links_feed/feed.xml"), "summary"));

		// the feed entries must leave untouched if the links can't be loaded.
		assertEquals(extractedResultFeedContent, extractedOriginalFeedContent);
	}
	
	/**
	 * Test for a page selector which did not match to the loaded page content. This must cause an empty feed entry content.
	 */
	@Test
	public void testBuildFailedInvalidNonMatchingPageSelector() throws Exception {
		String feed = new String(createFeedBuilder("feeds/valid_feed/feed.xml").loadFeed().expand("#unknown").build(), StandardCharsets.UTF_8);
		String extractedFeedContent = stripHtml(extractPageContent(feed, "summary"));
		
		// the feed entries must be empty if the selector did not match somewhere in the linked page.
		assertEquals(EMPTY, extractedFeedContent);
	}
	
	/**
	 * Test for a a feed which could not be loaded for some reason which must cause an {@link IOException}..
	 */	
	@Test(expected = IOException.class)
	public void testBuildFailedFeedDidNotExists() throws Exception {
		createFeedBuilder("not_existing.xml").loadFeed();
	}
	
	private SyndFeed createSyndFeed(String expandedFeed) throws FeedException, IOException {
		return new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(expandedFeed.getBytes())));
	}

	private String getHtmlPageContent(String page) throws IOException {
		return createUrlLoaderFactory().getUrlLoader("test://" + page).getContentAsString();
	}

	private String extractPageContent(String content1, String includeCssSelector) {
		List<String> extractPageElements = new PageContentExtractor(includeCssSelector)
				.extractPageElements(content1, "http://test.de")
				.getPageElements();
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
					bind(PageCache.class).toInstance(new DummyPageCache());
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
