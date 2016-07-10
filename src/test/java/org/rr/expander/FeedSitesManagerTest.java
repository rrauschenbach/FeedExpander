package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class FeedSitesManagerTest {
	
	private static final String SUCCESSFUL_CONFIG_LINE = "test | Test News | http://www.heise.de/newsticker/heise-atom.xml | article | 10"; 
	
	private static final String FLAWED_CONFIG_LINE_MISSING_PART = "test | http://www.heise.de/newsticker/heise-atom.xml | article | 10";
	
	private static final String COMMENT_LINE = "# comment here";
	
	public FeedSitesManager getFeedSitesManager(final String feedSites) {
		return new FeedSitesManager(EMPTY) {

			@Override
			protected Stream<String> getFeedSitesStream() throws IOException {
				return Stream.of(StringUtils.split(feedSites, '\n'));
			}
			
		};
	}
	
	@Test
	public void testSimpleSuccessfulCase() throws IOException {
		assertTrue(getFeedSitesManager(SUCCESSFUL_CONFIG_LINE).containsAlias("test"));
		assertEquals("Test News", getFeedSitesManager(SUCCESSFUL_CONFIG_LINE).getDescription("test"));
		assertEquals("http://www.heise.de/newsticker/heise-atom.xml", getFeedSitesManager(SUCCESSFUL_CONFIG_LINE).getFeedUrl("test"));
		assertEquals(Integer.valueOf(10), getFeedSitesManager(SUCCESSFUL_CONFIG_LINE).getLimit("test"));
		assertEquals("article", getFeedSitesManager(SUCCESSFUL_CONFIG_LINE).getSelector("test"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSimpleMissingConfigPart() throws IOException {
		getFeedSitesManager(FLAWED_CONFIG_LINE_MISSING_PART).size();
	}
	
	@Test
	public void testCommentSuccessfulCase() throws IOException {
		assertEquals(0, getFeedSitesManager(COMMENT_LINE).size());
	}
	
}
