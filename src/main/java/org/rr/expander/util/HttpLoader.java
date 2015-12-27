package org.rr.expander.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Fetch the content of a specified http url using the apache http client.
 */
public class HttpLoader {

	private static final int DEFAULT_TIMEOUT = 10000;

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
	public InputStream getContentAsStream() throws IOException {
		return new ByteArrayInputStream(getContent().getBytes(Charsets.UTF_8));
	}

	public String getContent() throws IOException {
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
		
		// do something useful with the response body
		// and ensure it is fully consumed
		byte[] byteArray = EntityUtils.toByteArray(entity);
		entity.consumeContent();
		
		String responseCharset = getResponseCharset(response);
		if(isNotBlank(responseCharset)) {
			return new String (byteArray, responseCharset);
		}
		
		return new String (byteArray);
	}

	private String getResponseCharset(HttpResponse response) {
		return trim(substringAfter(response.getFirstHeader("Content-Type").getValue(), "charset="));
	}

	private HttpGet createHttpGet(String url) {
		return new HttpGet(url);
	}

	private HttpClient createHttpClient(RequestConfig.Builder requestBuilder) {
		return HttpClientBuilder.create().setDefaultRequestConfig(requestBuilder.build()).build();
	}

	private RequestConfig.Builder createRequestBuilder(int timeout) {
		return RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout);
	}

}
