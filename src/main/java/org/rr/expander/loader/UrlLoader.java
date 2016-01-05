package org.rr.expander.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

/**
 * Specifies common methods to access some content from a URL. 
 */
public interface UrlLoader {

	/**
	 * Get the content of the url.
	 * 
	 * @param charset the charset which should be used to encode the content.
	 * @return the content as InputStream with the given encoding.
	 * @throws IOException
	 */
	@Nonnull InputStream getContentAsStream(@Nonnull Charset charset) throws IOException;

	/**
	 * Get the content of the url as {@link String}.
	 * 
	 * @return the content as {@link String}.
	 * @throws IOException 
	 */
	@Nonnull String getContentAsString() throws IOException;

}
