package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.views.View;

/**
 * The {@link View} provides data for the html page which makes it easier to create a FeedExpander url.
 */
public class ExpanderShowFeedsView extends View {
	
	private static final String URL_CREATOR_HTML_TEMPLATE = "show_feeds.ftl";

	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(ExpanderShowFeedsView.class);
	
	@Nonnull
	private final String serverUrl;
	
	@Nonnull
	private FeedSitesManager feedSitesManager;

	public ExpanderShowFeedsView(@Nonnull String serverUrl, @Nonnull FeedSitesManager feedSitesManager) {
		super(URL_CREATOR_HTML_TEMPLATE);
		this.serverUrl = serverUrl;
		this.feedSitesManager = feedSitesManager;
	}
	
	public Set<String> getFeedAliases() throws IOException {
		return feedSitesManager.getAliases();
	}
	
	public String getDescription(String alias) throws IOException {
		return feedSitesManager.getDescription(alias);
	}
	
	public String getFeedUrl(String alias) {
		return serverUrl + "/expand?alias=" + urlEncode(alias);
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
