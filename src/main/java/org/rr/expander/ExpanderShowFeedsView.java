package org.rr.expander;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.views.View;
/**
 * The {@link View} provides data for the html page which makes it easier to create a FeedExpander url.
 */
public class ExpanderShowFeedsView extends View {
	
	private static final String URL_CREATOR_HTML_TEMPLATE = "/web/show_feeds.ftl";

	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(ExpanderShowFeedsView.class);
	
	@Nonnull
	private final String serverUrl;
	
	@Nonnull
	private ExpanderFeedSitesManager feedSitesManager;
	
	@Nonnull
	private CreatorPageSitesManager creatorPageSitesManager;

	public ExpanderShowFeedsView(@Nonnull String serverUrl, @Nonnull ExpanderFeedSitesManager feedSitesManager,
			@Nonnull CreatorPageSitesManager creatorPageSitesManager) {
		super(URL_CREATOR_HTML_TEMPLATE);
		this.serverUrl = serverUrl;
		this.feedSitesManager = feedSitesManager;
		this.creatorPageSitesManager = creatorPageSitesManager;
	}
	
	public Set<String> getFeedAliases() throws IOException {
		return feedSitesManager.getAliases();
	}
	
	public Set<String> getPageAliases() throws IOException {
		return creatorPageSitesManager.getAliases();
	}
	
	public String getFeedDescription(String alias) throws IOException {
		return StringEscapeUtils.escapeHtml4(feedSitesManager.getDescription(alias));
	}
	
	public String getPageDescription(String alias) throws IOException {
		return StringEscapeUtils.escapeHtml4(creatorPageSitesManager.getDescription(alias));
	}
	
	public String getFeedUrl(String alias) {
		return prependHttp(serverUrl + "/expand?alias=" + urlEncode(alias));
	}
	
	public String getPageUrl(String alias) {
		return prependHttp(serverUrl + "/create?alias=" + urlEncode(alias));
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
			return URLEncoder.encode(value, UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			logger.error(String.format("Failed to url encode the value '%s'.", value));
		}
		return EMPTY;
	}

}
