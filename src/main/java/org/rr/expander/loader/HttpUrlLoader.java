package org.rr.expander.loader;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.upperCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Optional;
import java.util.regex.Pattern;

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

import com.google.common.annotations.VisibleForTesting;

/**
 * Fetch the content of a specified http or https url using the apache http client.
 */
class HttpUrlLoader implements UrlLoader {

	private static final int DEFAULT_TIMEOUT = 10000;
	
	private static final Pattern CHARSET_PATTERN = Pattern.compile(".*charset=([\\w-]*).*");
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(HttpUrlLoader.class);

	@Nonnull
	private String url;

	@Nullable
	private ByteArrayInputStream in;

	public HttpUrlLoader(@Nonnull String url) {
		this.url = url;
	}

	@Override
	public @Nonnull InputStream getContentAsStream(@Nonnull Charset charset) throws IOException {
		return new ByteArrayInputStream(getContentAsString().getBytes(charset));
	}

	@Override
	public @Nonnull String getContentAsString() throws IOException {
		HttpResponse httpResponse = validateStatusCode(getHttpResponse());
		HttpEntity entity = httpResponse.getEntity();
		byte[] responseBytes = EntityUtils.toByteArray(entity);
		IOUtils.closeQuietly(entity.getContent()); // ensure it is fully consumed	
		return getResponseAsString(httpResponse, responseBytes);
	}

	@VisibleForTesting
	protected @Nonnull HttpResponse getHttpResponse() throws IOException {
		RequestConfig.Builder requestBuilder = createRequestBuilder(DEFAULT_TIMEOUT);
		HttpClient client = createHttpClient(requestBuilder);
		HttpGet httpGet = createHttpGet(url);

		return client.execute(httpGet);
	}

	private HttpResponse validateStatusCode(HttpResponse response) throws IOException {
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException(response.getStatusLine().toString());
		}
		return response;
	}

	private String getResponseAsString(HttpResponse response, byte[] content) throws UnsupportedEncodingException {
		String charset = getResponseCharset(response);
		if(isValidCharset(charset)) {
			return new String(content, charset);
		}
		return new String(content, detectOrGetDefaultEncoding(content));
	}

	private @Nonnull String detectOrGetDefaultEncoding(@Nonnull byte[] content) {
		return Optional.ofNullable(detectEncoding(content)).orElse(Charset.defaultCharset().name());
	}
	
	private @Nullable String detectEncoding(@Nonnull byte[] content) {
    UniversalDetector detector = new UniversalDetector(null);
    try {
	    detector.handleData(content, 0, content.length);
	    detector.dataEnd();
	    String encoding = upperCase(detector.getDetectedCharset());
	    return isValidCharset(encoding) ? encoding : null;
    } finally {
    	detector.reset();
    }
  }
	
	private boolean isValidCharset(@Nullable String charset) {
		try {
			return isNotBlank(charset) && Charset.isSupported(charset);
		} catch(IllegalCharsetNameException e) {
			logger.warn(String.format("response from '%s' with unknown character encoding '%s' detected.", url, charset));
		}
		return false;
	}

	private @Nullable String getResponseCharset(@Nonnull HttpResponse response) {
		return Optional.ofNullable(response.getFirstHeader("Content-Type").getValue())
				.map(contentType -> CHARSET_PATTERN.matcher(contentType))
				.filter(matcher -> matcher.matches())
				.map(matcher -> matcher.group(1))
				.orElse(null);
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
