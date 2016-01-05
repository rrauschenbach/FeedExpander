package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.junit.Test;
import org.rr.expander.loader.UrlLoaderFactory;

import junit.framework.AssertionFailedError;

public class FeedBuilderTest {

	private static final String CONTENT_EXPRESSION = "id=main";

	@Test
	public void testBuildSuccess() throws Exception {
		String expandedFeed = new String(createFeedBuilder("feed_1.xml").loadFeed().expand(CONTENT_EXPRESSION).build(), StandardCharsets.UTF_8);
		String extractedPageContent1 = stripHtml(extractPageContent(getHtmlPageContent("content_1.html"), CONTENT_EXPRESSION));
		String extractedPageContent2 = stripHtml(extractPageContent(getHtmlPageContent("content_2.html"), CONTENT_EXPRESSION));
		
		assertTrue(contains(expandedFeed, extractedPageContent1));
		assertTrue(contains(expandedFeed, extractedPageContent2));
	}
	
	@Test(expected=IOException.class)
	public void testBuildFailedNonExistingFeed() throws Exception {
		new String(createFeedBuilder("not_existing.xml").loadFeed().expand(CONTENT_EXPRESSION).build(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void testBuildFailedInvalidFeed() throws Exception {
		String feed = new String(createFeedBuilder("invalid_feed.xml").loadFeed().build(), StandardCharsets.UTF_8);
		assertTrue(contains(feed, "<feed xmlns=\"http://www.w3.org/2005/Atom\" />")); // empty feed
	}

	@Test
	public void testBuildFailedInvalidFeedLinks() throws Exception {
		String feed = new String(createFeedBuilder("invalid_feed_links.xml").loadFeed().expand(CONTENT_EXPRESSION).build(), StandardCharsets.UTF_8);
		String extractedResultFeedContent = stripHtml(extractPageContent(feed, "tag=*summary"));
		String extractedOriginalFeedContent = stripHtml(extractPageContent(getHtmlPageContent("invalid_feed_links.xml"), "tag=*summary"));

		// the feed entries must leave untouched if the links can't be loaded.
		assertEquals(extractedResultFeedContent, extractedOriginalFeedContent);
	}
	
	@Test
	public void testBuildFailedInvalidNonMatchingPageExpression() throws Exception {
		String feed = new String(createFeedBuilder("feed_1.xml").loadFeed().expand("id=unknown").build(), StandardCharsets.UTF_8);
		String extractedFeedContent = stripHtml(extractPageContent(feed, "tag=*summary"));
		
		// the feed entries must be empty if the expression did not match somewhere in the linked page.
		assertEquals(EMPTY, extractedFeedContent);
	}
	
	@Test(expected=IOException.class)
	public void testBuildFailedFeedDidNotExists() throws Exception {
		createFeedBuilder("not_existing.xml").loadFeed();
	}
	
	private String getHtmlPageContent(String page) throws IOException {
		return getTestUrlLoaderFactory().getUrlLoader("test://" + page).getContentAsString();
	}

	private String extractPageContent(String content1, String includeExpression) {
		List<String> extractPageElements = new PageContentExtractor(includeExpression).extractPageElements(content1, "http://test.de");
		if(!extractPageElements.isEmpty()) {
			return extractPageElements.get(0);
		}
		throw new AssertionFailedError(String.format("Failed to load page %s and extract %s", content1, includeExpression));
	}
	
	private String stripHtml(String html) {
		return Jsoup.parse(html).text();
	}
	
	private FeedBuilder createFeedBuilder(String feed) {
		return new FeedBuilder("test://" + feed, getTestUrlLoaderFactory());
	}
	
	private UrlLoaderFactory getTestUrlLoaderFactory() {
		return new TestUrlLoaderFactory();
	}
	
}
