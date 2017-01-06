package org.rr.expander.feed;


import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HtmlUtilsTest {

	@Test
	public void testStripHtml() {
		String stripped = HtmlUtils.stripHtml("<p>test</p>");
		assertEquals("test", stripped);
	}
	
	@Test
	public void testStripHtmlPlainText() {
		String stripped = HtmlUtils.stripHtml("test");
		assertEquals("test", stripped);
	}
	
	@Test
	public void testStripHtmlEmpty() {
		String stripped = HtmlUtils.stripHtml(EMPTY);
		assertEquals(EMPTY, stripped);
	}
	
	@Test
	public void testCleanHtml() {
		String cleaned = HtmlUtils.cleanHtml("<javascript>echo 'Hello World';</javascript><p>text</p>");
		assertTrue(cleaned.contains("Hello World"));
		assertTrue(cleaned.contains("<p>text</p>"));
		assertFalse(cleaned.contains("javascript"));
	}
	
	@Test
	public void testCleanHtmlPlainText() {
		String cleaned = HtmlUtils.cleanHtml("test");
		assertEquals("test", cleaned);
	}
	
	@Test
	public void testCleanHtmlEmpty() {
		String cleaned = HtmlUtils.cleanHtml(EMPTY);
		assertEquals(EMPTY, cleaned);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithAnAbsoluteLink() {
		String pageUrl = "http://example.com/path/to/test.html";
		String absoluteLink = HtmlUtils.makeAbsolute(pageUrl, pageUrl);
		assertEquals(pageUrl, absoluteLink);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithRelativeLinkAndTrailingSlash() {
		String pageUrl = "http://example.com/path/to/";
		String absoluteLink = HtmlUtils.makeAbsolute("test.html", pageUrl);
		assertEquals(pageUrl + "test.html", absoluteLink);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithRelativeLinkAndNoTrailingSlash() {
		String pageUrl = "http://example.com/path/to";
		String absoluteLink = HtmlUtils.makeAbsolute("test.html", pageUrl);
		assertEquals("http://example.com/path/test.html", absoluteLink);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithAbsoluteLinkAndTrailingSlash() {
		String pageUrl = "http://example.com/path/to/";
		String absoluteLink = HtmlUtils.makeAbsolute("/test.html", pageUrl);
		assertEquals("http://example.com/test.html", absoluteLink);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithAbsoluteLinkAndNoTrailingSlash() {
		String pageUrl = "http://example.com/path/to";
		String absoluteLink = HtmlUtils.makeAbsolute("/test.html", pageUrl);
		assertEquals("http://example.com/test.html", absoluteLink);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithRelativeLinkAndNoPath() {
		String pageUrl = "http://example.com";
		String absoluteLink = HtmlUtils.makeAbsolute("test.html", pageUrl);
		assertEquals(pageUrl + "/test.html", absoluteLink);
	}
	
	@Test
	public void testMakeAbsoluteLinkWithAbsoluteLinkAndNoPath() {
		String pageUrl = "http://example.com/";
		String absoluteLink = HtmlUtils.makeAbsolute("/test.html", pageUrl);
		assertEquals(pageUrl + "test.html", absoluteLink);
	}
}
