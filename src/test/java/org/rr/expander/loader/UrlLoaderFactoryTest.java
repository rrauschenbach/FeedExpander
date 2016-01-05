package org.rr.expander.loader;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UrlLoaderFactoryTest {

	@Test
	public void testCreateHttpLoader() {
		UrlLoaderFactory urlLoaderFactory = UrlLoaderFactory.createURLLoaderFactory();
		UrlLoader urlLoader = urlLoaderFactory.getUrlLoader("http://test.de");
		assertNotNull(urlLoader);
	}
	
	@Test
	public void testCreateHttpsLoader() {
		UrlLoaderFactory urlLoaderFactory = UrlLoaderFactory.createURLLoaderFactory();
		UrlLoader urlLoader = urlLoaderFactory.getUrlLoader("https://test.de");
		assertNotNull(urlLoader);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateNotExistingLoader() {
		UrlLoaderFactory urlLoaderFactory = UrlLoaderFactory.createURLLoaderFactory();
		urlLoaderFactory.getUrlLoader("abcd://test.de");
	}
}
