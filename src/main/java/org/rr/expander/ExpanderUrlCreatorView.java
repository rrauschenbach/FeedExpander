package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.views.View;

/**
 * The {@link View} provides data for the html page which makes it easier to create a FeedExpander url.
 */
public class ExpanderUrlCreatorView extends View {
	
	private static final String URL_CREATOR_HTML_TEMPLATE = "url_creator.ftl";

	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(ExpanderUrlCreatorView.class);
	
	@Nullable
	private String feedUrl;
	
	@Nullable
	private String limit;
	
	@Nullable
	private String includeCssSelector;
	
	@Nonnull
	private final String serverUrl;

	public ExpanderUrlCreatorView(@Nonnull String serverUrl, @Nullable String feedUrl, @Nullable String limit,
			@Nullable String includeCssSelector) {
		super(URL_CREATOR_HTML_TEMPLATE);
		this.serverUrl = serverUrl;
		this.feedUrl = feedUrl;
		this.limit = limit;
		this.includeCssSelector= includeCssSelector;
	}
	
	public @Nonnull String getFinalFeedUrl() {
		String encodedFeedUrl = getEncodedFeedUrl();
		if(isNotBlank(encodedFeedUrl)) {
			return String.format("%s?feedUrl=%s&include=%s&limit=%s", 
					serverUrl, getEncodedFeedUrl(), getEncodedincludeCssSelector(), getLimit());
		}
		return EMPTY;
	}
	
	private @Nonnull String getEncodedFeedUrl() {
		return getEncodedValue(feedUrl);
	}
	
	public @Nonnull String getLimit() {
		return Optional.ofNullable(limit).orElse("10");
	}
	
	public @Nonnull String getincludeCssSelector() {
		return Optional.ofNullable(includeCssSelector).orElse(EMPTY);
	}
	
	public @Nonnull String getFeedUrl() {
		return Optional.ofNullable(feedUrl).orElse(EMPTY);
	}
	
	private @Nonnull String getEncodedincludeCssSelector() {
		return getEncodedValue(getincludeCssSelector());
	}
	
	private @Nonnull String getEncodedValue(@Nullable String value) {
		return Optional.ofNullable(value)
				.map(url -> prependHttp(url))
				.map(url -> urlEncode(url)).orElse(EMPTY);
	}

	/**
	 * Attach the http:// protocol part to the given url if it did not already contain it.
	 * 
	 * @param url The url to be tested having a http protocol part.
	 * @return The url with http protocol part.
	 */
	private @Nonnull String prependHttp(@Nonnull String url) {
		return url.startsWith("http") ? url : "http://" + url;
	}
	
	private @Nonnull String urlEncode(@Nonnull String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(String.format("Failed to url encode the value '%s'.", value));
		}
		return EMPTY;
	}

}
