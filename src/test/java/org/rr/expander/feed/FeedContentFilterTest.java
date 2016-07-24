package org.rr.expander.feed;


import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class FeedContentFilterTest {

	private FeedContentFilter createFeedContentFilter(String regex) {
		return new FeedContentFilterImpl(regex);
	}
	
	private List<SyndEntry> createEntries() {
		List<SyndEntry> result = new ArrayList<>();
		
		applyFeedEntry(result, "Peter", "TEST");
		applyFeedEntry(result, "Marc", "Some content");
		
		return result;
	}

	private void applyFeedEntry(List<SyndEntry> result, String author, String content) {
		SyndEntryImpl entry = new SyndEntryImpl();
		entry.setAuthor(author);
		
		SyndContentImpl desc = new SyndContentImpl();
		desc.setValue(content);
		entry.setDescription(desc);
		
		entry.setContents(Arrays.asList(desc));
		result.add(entry);
	}
	
	@Test
	public void testFilterIncludeOne() {
		FeedContentFilter feedContentFilter = createFeedContentFilter("test");
		List<SyndEntry> filterInclude = feedContentFilter.filterInclude(createEntries());
		assertTrue(filterInclude.size() == createEntries().size() -1);
	}
	
	
	@Test
	public void testFilterIncludeTwo() {
		FeedContentFilter feedContentFilter = createFeedContentFilter("(test|Some)");
		List<SyndEntry> filterInclude = feedContentFilter.filterInclude(createEntries());
		assertTrue(filterInclude.size() == createEntries().size() -2);
	}
	
	@Test
	public void testFilterExcludeToOne() {
		FeedContentFilter feedContentFilter = createFeedContentFilter("test");
		List<SyndEntry> filterInclude = feedContentFilter.filterExclude(createEntries());
		assertTrue(filterInclude.size() == createEntries().size() -1);
	}
	
	@Test
	public void testFilterExcludeToNone() {
		FeedContentFilter feedContentFilter = createFeedContentFilter("(test|Some)");
		List<SyndEntry> filterInclude = feedContentFilter.filterExclude(createEntries());
		assertTrue(filterInclude.size() == createEntries().size());
	}
	
	@Test
	public void testFilterExcludeEmpty() {
		FeedContentFilter feedContentFilter = createFeedContentFilter(EMPTY);
		List<SyndEntry> filterInclude = feedContentFilter.filterExclude(createEntries());
		assertTrue(filterInclude.size() == createEntries().size());
	}
	
	
	@Test
	public void testFilterIncludeEmpty() {
		FeedContentFilter feedContentFilter = createFeedContentFilter(EMPTY);
		List<SyndEntry> filterInclude = feedContentFilter.filterInclude(createEntries());
		assertTrue(filterInclude.size() == createEntries().size());
	}
}
