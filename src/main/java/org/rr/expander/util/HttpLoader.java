package org.rr.expander.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Fetch the content of a specified http url using the apache http client.
 */
public class HttpLoader {

	private static final int DEFAULT_TIMEOUT = 10000;
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(HttpLoader.class);

	@Nonnull
	private String url;

	@Nullable
	private ByteArrayInputStream in;

	public HttpLoader(@Nonnull String url) {
		this.url = url;
	}
	
	/**
	 * @return the content as InputStream with UTF-8 encoding.
	 * @throws IOException
	 */
	public @Nonnull InputStream getContentAsStream() throws IOException {
		return new ByteArrayInputStream(getContent().getBytes(StandardCharsets.UTF_8));
	}

	public @Nonnull String getContent() throws IOException {
		RequestConfig.Builder requestBuilder = createRequestBuilder(DEFAULT_TIMEOUT);
		HttpClient client = createHttpClient(requestBuilder);
		HttpGet httpGet = createHttpGet(url);

		HttpResponse response = client.execute(httpGet);

		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network
		// socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST either fully consume the response content or abort request
		// execution by calling HttpGet#releaseConnection().
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException(response.getStatusLine().toString());
		}
		
		HttpEntity entity = response.getEntity();
		byte[] responseBytes = EntityUtils.toByteArray(entity);
		IOUtils.closeQuietly(entity.getContent()); // ensure it is fully consumed	
		return getResponseAsString(response, responseBytes);
	}

	private String getResponseAsString(HttpResponse response, byte[] content) throws UnsupportedEncodingException {
		String charset = getResponseCharset(response);
		if(isValidCharset(charset)) {
			return new String(content, charset);
		}
		return new String(content, detectOrGetDefaultEncoding(content));
	}
	
	private @Nonnull String detectOrGetDefaultEncoding(@Nonnull byte[] content) {
		return Optional.<String>fromNullable(detectEncoding(content)).or(Charset.defaultCharset().name());
	}
	
	private @Nullable String detectEncoding(@Nonnull byte[] content) {
    UniversalDetector detector = new UniversalDetector(null);
    try {
	    detector.handleData(content, 0, content.length);
	    detector.dataEnd();
	    String encoding = detector.getDetectedCharset();
	    return isValidCharset(encoding) ? encoding : null;
    } finally {
    	detector.reset();
    }
  }
	
	private boolean isValidCharset(String charset) {
		try {
			return isNotBlank(charset) && Charset.isSupported(charset);
		} catch(IllegalCharsetNameException e) {
			logger.warn(String.format("response with unknown character encoding '%s' detected.", charset));
		}
		return false;
	}

	private @Nullable String getResponseCharset(HttpResponse response) {
		return trim(substringAfter(response.getFirstHeader("Content-Type").getValue(), "charset="));
	}

	private @Nonnull HttpGet createHttpGet(String url) {
		return new HttpGet(url);
	}

	private @Nonnull HttpClient createHttpClient(@Nonnull RequestConfig.Builder requestBuilder) {
		return HttpClientBuilder.create().setDefaultRequestConfig(requestBuilder.build()).build();
	}

	private @Nonnull RequestConfig.Builder createRequestBuilder(int timeout) {
		return RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout);
	}

}
