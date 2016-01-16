package org.rr.expander.cache;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class BZip2MemPageCacheImplTest {

	private static final String DEFAULT_PAGE_CONTENT = "<html><head></head><body></body></html>";

	@Test
	public void testCompressAndDecrompess() throws IOException {
		BZip2MemPageCacheImpl cache = new BZip2MemPageCacheImpl(10);
		byte[] compressed = cache.compress(DEFAULT_PAGE_CONTENT);
		String decompressed = cache.decompress(compressed);
		assertEquals(DEFAULT_PAGE_CONTENT, decompressed);
	}

	@Test
	public void testCompression() throws IOException {
		BZip2MemPageCacheImpl cache = new BZip2MemPageCacheImpl(10);
		byte[] compressed = cache.compress(DEFAULT_PAGE_CONTENT);
		assertEquals("BZ", new String(compressed, 0, 2));
	}

	@Test(expected = NullPointerException.class)
	public void testCompressWithNull() throws IOException {
		BZip2MemPageCacheImpl cache = new BZip2MemPageCacheImpl(10);
		cache.compress(null);
	}

	@Test(expected = NullPointerException.class)
	public void testDecompressWithNull() throws IOException {
		BZip2MemPageCacheImpl cache = new BZip2MemPageCacheImpl(10);
		cache.decompress(null);
	}
	
	@Test
	public void testCompressWithEmptyString() throws IOException {
		BZip2MemPageCacheImpl cache = new BZip2MemPageCacheImpl(10);
		byte[] compressed = cache.compress(EMPTY);
		assertEquals("BZ", new String(compressed, 0, 2));
	}
	
	@Test(expected = IOException.class)
	public void testDecompressWithEmptyByteArray() throws IOException {
		BZip2MemPageCacheImpl cache = new BZip2MemPageCacheImpl(10);
		cache.decompress(new byte[0]);
	}
}
