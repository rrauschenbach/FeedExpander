package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonMappingException;

public class FeedSitesManagerTest {
	
	private static String TWO_ENTRIES_CONFIG = "feeds:\n"
			+ "- alias: test\n"
			+ "  description: News for testing\n"
			+ "  feedUrl: http://www.test.de/newsticker/rss.xml\n"
			+ "  selector: 'article'\n"
			+ "  limit: 10\n"
			+ "  filter:\n"
			+ "  - include: 'qwertz'\n"
			+ "    exclude: 'ztrewq'\n"
			
			+ "- alias: dummy\n"
			+ "  description: Dummy News\n"
			+ "  feedUrl: http://www.dummy.de/newsticker/rss.xml\n"
			+ "  selector: 'article'\n"
			+ "  limit: 10\n";
	
	public FeedSitesManager getFeedSitesManager(String feedsConfig) {
		return new FeedSitesManager(EMPTY) {

			@Override
			protected String readFeedSitesConfig(@Nonnull Path feedSitesFile) throws IOException {
				return feedsConfig; 
			}
			
		};
	}
	
	@Test
	public void testDefaultConfigFileContainsExampleEntries() throws IOException {
		FeedSitesManager feedSitesManager = getFeedSitesManager(FileUtils.readFileToString(new File("feed-config.yml")));
		assertTrue(feedSitesManager.size() > 0);
	}
	
	@Test(expected = JsonMappingException.class)
	public void testEmptyContent() throws IOException {
		FeedSitesManager feedSitesManager = getFeedSitesManager(EMPTY);
		feedSitesManager.size();
	}
	
	@Test(expected = JsonMappingException.class)
	public void testInvalidContent() throws IOException {
		FeedSitesManager feedSitesManager = getFeedSitesManager(EMPTY);
		feedSitesManager.size();
	}
	
	@Test
	public void testValidConfig() throws IOException {
		FeedSitesManager feedSitesManager = getFeedSitesManager(TWO_ENTRIES_CONFIG);
		assertTrue(feedSitesManager.size() == 2);
		
		for (String alias : feedSitesManager.getAliases()) {
			assertTrue(isNotBlank(alias));
			assertTrue(TWO_ENTRIES_CONFIG.contains(alias));
			assertTrue(TWO_ENTRIES_CONFIG.contains(feedSitesManager.getDescription(alias)));
			assertTrue(TWO_ENTRIES_CONFIG.contains(feedSitesManager.getFeedUrl(alias)));
			assertTrue(TWO_ENTRIES_CONFIG.contains(feedSitesManager.getSelector(alias)));
			assertTrue(TWO_ENTRIES_CONFIG.contains(String.valueOf(feedSitesManager.getLimit(alias))));
		}
	}
	
	@Test
	public void testFilterEntry() throws IOException {
		FeedSitesManager feedSitesManager = getFeedSitesManager(TWO_ENTRIES_CONFIG);
		assertTrue(feedSitesManager.size() == 2);
		
		List<String> excludeFilter = feedSitesManager.getExcludeFilter("test");
		assertTrue(!excludeFilter.isEmpty());
		assertEquals(excludeFilter.get(0), "ztrewq");
		
		List<String> includeFilter = feedSitesManager.getIncludeFilter("test");
		assertTrue(!includeFilter.isEmpty());
		assertEquals(includeFilter.get(0), "qwertz");
	}

	
}
