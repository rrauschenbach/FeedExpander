package org.rr.expander.cache;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class CompressedMemPageCacheTest {

	private static final String DEFAULT_PAGE_CONTENT = "<html><head></head><body></body></html>";

	@Test
	public void testStoreFetchSuccess() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String url = "http://test.de";

		cache.store(url, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(url);
		assertEquals(DEFAULT_PAGE_CONTENT, restoredPageContent);
	}

	@Test
	public void testStoreFetchWithHttpsInsteadOfHttpSuccess() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String httpUrl = "http://test.de";
		String httpsUrl = "https://test.de";

		cache.store(httpUrl, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(httpsUrl);
		assertEquals(DEFAULT_PAGE_CONTENT, restoredPageContent);
	}

	@Test
	public void testStoreFetchWithNormalizedUrlSuccess() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String httpUrl1 = "http://test.de/";
		String httpUrl2 = "http://test.de";

		cache.store(httpUrl1, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(httpUrl2);
		assertEquals(DEFAULT_PAGE_CONTENT, restoredPageContent);
	}

	@Test
	public void testStoreFetchWithDifferentDomains() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String httpUrl1 = "http://test.de";
		String httpUrl2 = "http://test.com";

		cache.store(httpUrl1, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(httpUrl2);
		assertNull(restoredPageContent);
	}
	
	@Test
	public void testStoreFetchWithDifferentPort() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String httpUrl80 = "http://test.de:80";
		String httpUrl90 = "http://test.de:90";
		
		cache.store(httpUrl80, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(httpUrl90);
		
		assertNull(restoredPageContent);
	}
	
	@Test
	public void testStoreFetchWithDifferentPathParameter() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String httpUrl1 = "http://www.example.com/folder/exist?name=sky";
		String httpUrl2 = "http://www.example.com/folder/exist?name=hell";
		
		cache.store(httpUrl1, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(httpUrl2);
		
		assertNull(restoredPageContent);
	}
	
	@Test
	public void testStoreFetchWithEqualPathParameter() {
		CompressedMemPageCache cache = createCompressedMemPageCache(10);
		String httpUrl1 = "http://www.example.com/folder/exist?name=sky";
		
		cache.store(httpUrl1, DEFAULT_PAGE_CONTENT);
		String restoredPageContent = cache.restore(httpUrl1);
		
		assertEquals(DEFAULT_PAGE_CONTENT, restoredPageContent);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCompressedMemPageCacheWithInvalidMaxSize() {
		createCompressedMemPageCache(0);
	}

	private CompressedMemPageCache createCompressedMemPageCache(int maxSize) {
		return new CompressedMemPageCache(maxSize) {

			@Override
			protected String decompress(byte[] compressed) throws IOException {
				return new String(compressed, StandardCharsets.UTF_8);
			}

			@Override
			protected byte[] compress(String pageContent) throws IOException {
				return pageContent.getBytes(StandardCharsets.UTF_8);
			}
		};
	}
}
