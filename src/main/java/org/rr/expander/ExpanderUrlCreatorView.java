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
	private String includeExpression;
	
	@Nonnull
	private final String serverUrl;

	public ExpanderUrlCreatorView(@Nonnull String serverUrl, @Nullable String feedUrl, @Nullable String limit,
			@Nullable String includeExpression) {
		super(URL_CREATOR_HTML_TEMPLATE);
		this.serverUrl = serverUrl;
		this.feedUrl = feedUrl;
		this.limit = limit;
		this.includeExpression= includeExpression;
	}
	
	public @Nonnull String getFinalFeedUrl() {
		String encodedFeedUrl = getEncodedFeedUrl();
		if(isNotBlank(encodedFeedUrl)) {
			return String.format("%s?feedUrl=%s&include=%s&limit=%s", 
					serverUrl, getEncodedFeedUrl(), getEncodedIncludeExpression(), getLimit());
		}
		return EMPTY;
	}
	
	private @Nonnull String getEncodedFeedUrl() {
		return getEncodedValue(feedUrl);
	}
	
	public @Nonnull String getLimit() {
		return Optional.ofNullable(limit).orElse("10");
	}
	
	public @Nonnull String getIncludeExpression() {
		return includeExpression;
	}
	
	public @Nonnull String getFeedUrl() {
		return feedUrl;
	}
	
	private @Nonnull String getEncodedIncludeExpression() {
		return getEncodedValue(getIncludeExpression());
	}
	
	private @Nonnull String getEncodedValue(@Nullable String value) {
		return Optional.ofNullable(value)
				.map(url -> urlEncode(url)).orElse(EMPTY);
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
