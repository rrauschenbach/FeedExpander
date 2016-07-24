package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonMappingException;

public class FeedSitesManagerTest {
	
	private static String TWO_ENTRIES_CONFIG = "{\"feeds\":[{\"alias\":\"heise\",\"description\":\"Heise news\",\"feedUrl\":\"http://www.heise.de/newsticker/heise-atom.xml\",\"selector\":\"article\",\"limit\":10},{\"alias\":\"golem\",\"description\":\"Golem news\",\"feedUrl\":\"http://rss.golem.de/rss.php?feed=RSS2.0\",\"selector\":\"article\",\"limit\":11}]}";
	
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
		FeedSitesManager feedSitesManager = getFeedSitesManager(FileUtils.readFileToString(new File("expand-feeds.config")));
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
			assertTrue(TWO_ENTRIES_CONFIG.contains(quote(alias)));
			assertTrue(TWO_ENTRIES_CONFIG.contains(quote(feedSitesManager.getDescription(alias))));
			assertTrue(TWO_ENTRIES_CONFIG.contains(quote(feedSitesManager.getFeedUrl(alias))));
			assertTrue(TWO_ENTRIES_CONFIG.contains(quote(feedSitesManager.getSelector(alias))));
			assertTrue(TWO_ENTRIES_CONFIG.contains(String.valueOf(feedSitesManager.getLimit(alias))));
		}
	}
	
	private static String quote(String s) {
		return "\"" + s + "\"";
	}
	
}
