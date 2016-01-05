package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rr.expander.loader.UrlLoaderFactory;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class FeedContentExchangerTest {

	@Test
	public void testFeedContentExchangerSuccess() {
		List<SyndEntry> entries = createValidLinkedEntriesWithEmptyDescription(EMPTY);
		createFeedContentExchanger("id=main").exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must no longer be empty
			assertTrue(isNotBlank(entry.getDescription().getValue()));
		}
	}

	@Test
	public void testFeedContentExchangerFailWithNoMatchingExpression() {
		List<SyndEntry> entries = createValidLinkedEntriesWithEmptyDescription("Test");
		createFeedContentExchanger("id=not_exists").exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must be empty because nothing has matched in the web page
			assertTrue(isBlank(entry.getDescription().getValue()));
		}
	}

	@Test
	public void testFeedContentExchangerFailWithInvalidLinks() {
		List<SyndEntry> entries = createInvalidLinkedEntriesWithTestDescription();
		createFeedContentExchanger("id=not_exists").exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must stay unchanged because no page content could be loaded.
			assertEquals("Test", entry.getDescription().getValue());
		}
	}
	
	@Test
	public void testFeedContentExchangerWithNullExpression() {
		List<SyndEntry> entries = createValidLinkedEntriesWithEmptyDescription("Test");
		createFeedContentExchanger(null).exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must be empty because nothing has matched in the web page
			assertTrue(isBlank(entry.getDescription().getValue()));
		}
	}
	
	@Test
	public void testFeedContentExchangerWithNullEntries() {
		// do nothing but log a NullPointerException
		createFeedContentExchanger(null).exchangeAll(null);
	}

	private List<SyndEntry> createInvalidLinkedEntriesWithTestDescription() {
		SyndEntry entry1 = new SyndEntryImpl();
		entry1.setLink("test://not_exists1.html");
		SyndContentImpl syndContent1 = new SyndContentImpl();
		syndContent1.setValue("Test");
		entry1.setDescription(syndContent1);

		SyndEntry entry2 = new SyndEntryImpl();
		entry2.setLink("test://not_exists2.html");
		SyndContentImpl syndContent2 = new SyndContentImpl();
		syndContent2.setValue("Test");
		entry2.setDescription(syndContent2);

		return Arrays.asList(entry1, entry2);
	}

	private List<SyndEntry> createValidLinkedEntriesWithEmptyDescription(String content) {
		SyndEntry entry1 = new SyndEntryImpl();
		entry1.setLink("test://content_1.html");
		SyndContentImpl syndContent1 = new SyndContentImpl();
		syndContent1.setValue(content);
		entry1.setDescription(syndContent1);

		SyndEntry entry2 = new SyndEntryImpl();
		entry2.setLink("test://content_2.html");
		SyndContentImpl syndContent2 = new SyndContentImpl();
		syndContent2.setValue(content);
		entry2.setDescription(syndContent2);

		return Arrays.asList(entry1, entry2);
	}

	private FeedContentExchanger createFeedContentExchanger(String includeExpression) {
		return new FeedContentExchanger(includeExpression, getTestUrlLoaderFactory());
	}

	private UrlLoaderFactory getTestUrlLoaderFactory() {
		return new TestUrlLoaderFactory();
	}
}
