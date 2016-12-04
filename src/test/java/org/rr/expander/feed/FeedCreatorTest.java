package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;
import org.rr.expander.loader.UrlLoaderFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedCreatorTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateSuccess() throws Exception {
		String createdFeed = new String(createFeedCreator("feeds/valid_page/index.html")
				.createFeed(".headline,.article,.footer", ".headline", "a", ".author").build());
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(createdFeed.getBytes())));
		List<SyndEntry> entries = feed.getEntries();

		assertEquals("headline1", entries.get(0).getTitle());
		assertEquals("http://dummy.url", entries.get(0).getLink());
		assertEquals("Jon Doe", entries.get(0).getAuthor());

		assertEquals("headline2", entries.get(1).getTitle());
		assertEquals("http://dummy.url", entries.get(1).getLink());
		assertEquals("Anna Smith", entries.get(1).getAuthor());
		
		String content1 = entries.get(0).getDescription().getValue();
		assertTrue(content1.contains("headline1"));
		assertTrue(content1.contains("text1"));
		assertTrue(content1.contains("footer1"));
		assertTrue(content1.contains("http://dummy.url"));

		String content2 = entries.get(1).getDescription().getValue();
		assertTrue(content2.contains("headline2"));
		assertTrue(content2.contains("text2"));
		assertTrue(content2.contains("footer2"));
		assertTrue(content2.contains("http://dummy.url"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateInvalidSelectorConfid() throws Exception {
		createFeedCreator("feeds/valid_page/index.html").createFeed(".headline,.article,.footer,.footer", ".headline", "a", ".author");
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCreateNullHeadlineAndLink() throws Exception {
		String createdFeed = new String(createFeedCreator("feeds/valid_page/index.html")
				.createFeed(".headline,.article,.footer", null, null, null)
				.setTitle("Title")
				.build());
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(createdFeed.getBytes())));
		assertEquals("Title", feed.getTitle());
		
		List<SyndEntry> entries = feed.getEntries();

		assertEquals(null, entries.get(0).getTitle());
		assertEquals(null, entries.get(0).getLink());
		assertEquals(EMPTY, entries.get(0).getAuthor());
		assertEquals(null, entries.get(1).getTitle());
		assertEquals(null, entries.get(1).getLink());
		assertEquals(EMPTY, entries.get(1).getAuthor());
	}

	private FeedCreator createFeedCreator(String feed) {
		return createInjector().getInstance(FeedCreatorFactory.class).createFeedBuilder("test://" + feed);
	}

	private TestUrlLoaderFactory createUrlLoaderFactory() {
		return new TestUrlLoaderFactory();
	}

	private Injector createInjector() {
		return Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindUrlLoaderFactory();
				bindFeedCreator();
			}

			private void bindUrlLoaderFactory() {
				bind(UrlLoaderFactory.class).toInstance(createUrlLoaderFactory());
			}

			private void bindFeedCreator() {
				install(new FactoryModuleBuilder().implement(FeedCreator.class, FeedCreatorImpl.class)
						.build(FeedCreatorFactory.class));
			}

		});
	}

}
