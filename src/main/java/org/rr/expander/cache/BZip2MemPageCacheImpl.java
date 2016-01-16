package org.rr.expander.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * {@link PageCache} implementation which uses bzip2 to store the page content compressed and having
 * a fixed maximum size. The least used pages will automatically removed from the cache.
 */
class BZip2MemPageCacheImpl extends CompressedMemPageCache {
	
	public BZip2MemPageCacheImpl(int maxSize) {
		super(maxSize);
	}

	@Override
	protected @Nonnull byte[] compress(@Nonnull String pageContent) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (OutputStream bzOut = new BZip2CompressorOutputStream(result)){
			IOUtils.write(pageContent.getBytes(StandardCharsets.UTF_8), bzOut);
		}
		return result.toByteArray();
	}
	
	@Override
	protected @Nonnull String decompress(@Nonnull byte[] compressed) throws IOException {
		try (InputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed))) {
			byte[] decompressed = IOUtils.toByteArray(bzIn);
			return new String(decompressed, StandardCharsets.UTF_8);
		}
	}

}
