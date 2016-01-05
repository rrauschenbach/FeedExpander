package org.rr.expander.loader;

import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rr.expander.loader.HttpUrlLoader;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class HttpLoaderTest {
	
	private static final String SOME_SPECIAL_CHARACTERS = "\u20ac\u00c4";

	private static final String EXAMPLE_FEED_URL = "http://some.feed.de/path";

	private static final String UTF8_HTML_CONTENT_TYPE = "text/html;charset=utf-8";
	
	private static final String ISO_HTML_CONTENT_TYPE = "text/html;charset=iso-8859-1";

	@Test
	public void testSuccessHttpLoaderWithUtf8() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.UTF_8);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setContentType(UTF8_HTML_CONTENT_TYPE);
		String response = httpLoaderTestImpl.getContentAsString();
		assertEquals(new String(exampleContent, StandardCharsets.UTF_8), response);
	}
	
	@Test
	public void testSuccessHttpLoaderWithIso() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.ISO_8859_1);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setContentType(ISO_HTML_CONTENT_TYPE);
		String response = httpLoaderTestImpl.getContentAsString();
		assertEquals(new String(exampleContent, StandardCharsets.ISO_8859_1), response);
	}
	
	@Test
	public void testHttpLoaderWithIsoButUtf8EncodedContent() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.UTF_8);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setContentType(ISO_HTML_CONTENT_TYPE);
		String response = httpLoaderTestImpl.getContentAsString();
		
		// the wrong encoding from the http header must be used.
		assertEquals(new String(exampleContent, StandardCharsets.ISO_8859_1), response);
	}
	
	@Test
	public void testHttpLoaderWithAutomaticallyIsoEncodedContent() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.ISO_8859_1);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent);
		String response = httpLoaderTestImpl.getContentAsString();
		
		// the automatically detected must be used.
		assertEquals(new String(exampleContent, StandardCharsets.ISO_8859_1), response);
	}
	
	@Test
	public void testHttpLoaderWithAutomaticallyUtf8EncodedContent() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.UTF_8);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent);
		String response = httpLoaderTestImpl.getContentAsString();
		
		// the automatically detected must be used.
		assertEquals(new String(exampleContent, StandardCharsets.UTF_8), response);
	}
	
	@Test
	public void testHttpLoaderWithUtf8ContentTypeStream() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.UTF_8);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setContentType(UTF8_HTML_CONTENT_TYPE);
		InputStream response = httpLoaderTestImpl.getContentAsStream(StandardCharsets.UTF_8);
		assertEquals(new String(exampleContent, StandardCharsets.UTF_8),
				IOUtils.toString(response, StandardCharsets.UTF_8));
	}
	
	@Test
	public void testHttpLoaderWithIsoContentTypeStream() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.ISO_8859_1);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setContentType(ISO_HTML_CONTENT_TYPE);
		InputStream response = httpLoaderTestImpl.getContentAsStream(StandardCharsets.UTF_8);
		
		// the response stream must be always utf-8 encoded
		assertEquals(new String(exampleContent, StandardCharsets.ISO_8859_1),
				IOUtils.toString(response, StandardCharsets.UTF_8));
	}
	
	@Test
	public void testHttpLoaderWithIsoResultStream() throws IOException {
		byte[] exampleContent = createExampleContent(StandardCharsets.ISO_8859_1);
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setContentType(ISO_HTML_CONTENT_TYPE);
		InputStream response = httpLoaderTestImpl.getContentAsStream(StandardCharsets.ISO_8859_1);
		
		// the response stream must be always utf-8 encoded
		assertEquals(new String(exampleContent, StandardCharsets.ISO_8859_1),
				IOUtils.toString(response, StandardCharsets.ISO_8859_1));
	}
	
	@Test(expected=IOException.class)
	@Parameters({ 
		"500", "404", "302", "204"
		})	
	public void testHttpLoaderWithStatusCodeOtherThan200AsStream(String statusCode) throws IOException {
		byte[] exampleContent = new byte[0];
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setStatusCode(toInt(statusCode));
		httpLoaderTestImpl.getContentAsStream(StandardCharsets.UTF_8);
	}
	
	@Test(expected=IOException.class)
	@Parameters({ 
		"500", "404", "302", "204"
	})	
	public void testHttpLoaderWithStatusCodeOtherThan200AsString(String statusCode) throws IOException {
		byte[] exampleContent = new byte[0];
		HttpLoaderTestImpl httpLoaderTestImpl = new HttpLoaderTestImpl(EXAMPLE_FEED_URL)
				.setContent(exampleContent)
				.setStatusCode(toInt(statusCode));
		httpLoaderTestImpl.getContentAsString();
	}

	private static byte[] createExampleContent(Charset charset) {
		return ("<html><head></head><body><p>" + SOME_SPECIAL_CHARACTERS + "</p></body></html>").getBytes(charset);
	}

	private class HttpLoaderTestImpl extends HttpUrlLoader {

		private byte[] content;

		private String contentType;
		
		private int statusCode = 200;

		public HttpLoaderTestImpl(String url) {
			super(url);
		}

		public HttpLoaderTestImpl setContent(byte[] content) {
			this.content = content;
			return this;
		}

		public HttpLoaderTestImpl setContentType(String contentType) {
			this.contentType = contentType;
			return this;
		}
		
		public HttpLoaderTestImpl setStatusCode(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		@Override
		protected HttpResponse getHttpResponse() throws IOException {
			ProtocolVersion protocol = new ProtocolVersion("HTTP", 1, 1);
			BasicStatusLine statusLine = new BasicStatusLine(protocol, statusCode, "test reason");
			BasicHttpResponse response = new BasicHttpResponse(statusLine);
			response.addHeader("Content-Type", contentType);
			response.addHeader("Allow", "POST");
			response.addHeader("Allow", "GET");

			BasicHttpEntity entity = new BasicHttpEntity();
			entity.setContent(new ByteArrayInputStream(content));
			response.setEntity(entity);
			return response;
		}
	}

}
