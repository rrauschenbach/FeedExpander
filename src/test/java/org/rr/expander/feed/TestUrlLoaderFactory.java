package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.rr.expander.loader.UrlLoader;
import org.rr.expander.loader.UrlLoaderFactory;

/**
 * A {@link UrlLoaderFactory} implementation which loads the feed and page content from the resource folder.
 */
public class TestUrlLoaderFactory extends UrlLoaderFactory {

	private static final String TEST_URL_PREFIX = "test://";

	@Override
	public UrlLoader getUrlLoader(String url) {
		return new UrlLoader() {
			
			@Override
			public String getContentAsString() throws IOException {
				return IOUtils.toString(getContentAsStream(StandardCharsets.UTF_8));
			}
			
			@Override
			public InputStream getContentAsStream(Charset charset) throws IOException {
				if(startsWith(url, TEST_URL_PREFIX)) {
					String fileName = substringAfter(url, TEST_URL_PREFIX);
					InputStream resultStream = getClass().getResourceAsStream("/"  + fileName);
					if(resultStream != null) {
						return resultStream;
					}
				}
				throw new IOException("Failed to load " + url);
			}
		};
	}

}
