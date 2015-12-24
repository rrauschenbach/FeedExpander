package org.rr.expander.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * InputStream implementation which is able to load http and https content using
 * the apache http client.
 */
public class HttpInputStream extends InputStream {

	@Nonnull
	private String url;

	@Nullable
	private ByteArrayInputStream in;

	public HttpInputStream(@Nonnull String url) {
		this.url = url;
	}

	private InputStream getInternalInputStream() throws IOException {
		if (in == null) {
			in = new ByteArrayInputStream(getContent(url));
		}
		return in;
	}

	@Override
	public int read() throws IOException {
		try {
			return getInternalInputStream().read();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private byte[] getContent(String url) throws IOException {
		RequestConfig.Builder requestBuilder = createRequestBuilder(10000);
		HttpClient client = createHttpClient(requestBuilder);
		HttpGet httpGet = createHttpGet(url);

		HttpResponse response1 = client.execute(httpGet);

		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network
		// socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST either fully consume the response content or abort request
		// execution by calling HttpGet#releaseConnection().

		if (response1.getStatusLine().getStatusCode() != 200) {
			throw new IOException(response1.getStatusLine().toString());
		}
		HttpEntity entity = response1.getEntity();

		// do something useful with the response body
		// and ensure it is fully consumed
		byte[] byteArray = EntityUtils.toByteArray(entity);
		entity.consumeContent();
		return byteArray;
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
