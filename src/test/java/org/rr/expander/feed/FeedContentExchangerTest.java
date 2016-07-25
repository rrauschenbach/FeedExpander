package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rr.expander.cache.PageCache;
import org.rr.expander.loader.UrlLoaderFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class FeedContentExchangerTest {

	@Test
	public void testFeedContentExchangerSuccess() {
		List<SyndEntry> entries = createValidLinkedEntriesWithEmptyDescription(EMPTY);
		createFeedContentExchanger("#main").exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must no longer be empty
			assertTrue(isNotBlank(entry.getDescription().getValue()));
		}
	}

	@Test
	public void testFeedContentExchangerFailWithNoMatchingExpression() {
		List<SyndEntry> entries = createValidLinkedEntriesWithEmptyDescription("Test");
		createFeedContentExchanger("#not_exists").exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must be empty because nothing has matched in the web page
			assertTrue(isBlank(entry.getDescription().getValue()));
		}
	}

	@Test
	public void testFeedContentExchangerFailWithInvalidLinks() {
		List<SyndEntry> entries = createInvalidLinkedEntriesWithTestDescription();
		createFeedContentExchanger("#not_exists").exchangeAll(entries);
		for (SyndEntry entry : entries) {
			// description must stay unchanged because no page content could be loaded.
			assertEquals("Test", entry.getDescription().getValue());
		}
	}
	
	@Test
	public void testFeedContentExchangerWithNullEntries() {
		// do nothing but log a NullPointerException
		createFeedContentExchanger(EMPTY).exchangeAll(null);
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

	private FeedContentExchanger createFeedContentExchanger(String includeCssSelector) {
		return createInjector().getInstance(FeedContentExchangerFactory.class).createFeedContentExchanger(includeCssSelector);
	}
	
	private DummyPageCache createDummyPageCache() {
		return new DummyPageCache();
	}

	private TestUrlLoaderFactory createTestUrlLoaderFactory() {
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
					bind(UrlLoaderFactory.class).toInstance(createTestUrlLoaderFactory());
				}

				private void bindPageCache() {
					bind(PageCache.class).toInstance(createDummyPageCache());
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
