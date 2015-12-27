package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.rr.expander.feed.FeedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;

@Path("/expand")
public class ExpanderResource {
	
	@Nonnull
	final static Logger logger = LoggerFactory.getLogger(ExpanderResource.class);
	
	@Nullable
	private String feedWhiteList;
	
	public ExpanderResource(@Nullable String feedWhiteList) {
		this.feedWhiteList = feedWhiteList;
	}

	@PermitAll
	@GET
	public Response expand(
			final @QueryParam("feedUrl") Optional<String> feedUrl,
			final @QueryParam("include") Optional<String> include) {
		return feedUrl.transform(new Function<String, Response>() {
			@Override
			public Response apply(String feedUrl) {
				try {
					if (isFeedAllowed(feedUrl)) {
						FeedBuilder feedHandler = new FeedBuilder(feedUrl).loadFeed().expand(include.or("*"));
						return Response.ok(feedHandler.build(), feedHandler.getMimeType()).build();
					}
					logger.warn(String.format("Fetching feed '%s' is not allowed.", feedUrl));
					return getForbiddenResponse();
				} catch (Exception e) {
					logger.warn(String.format("Fetching feed '%s' has failed.", feedUrl), e);
					return getInternalServerErrorResponse();
				}
			}
		}).or(getBadRequestResponse()); // (no feedUrl)
	}

	private boolean isFeedAllowed(@Nonnull String feedUrl) {
		if (isNotBlank(feedWhiteList)) {
			return new UrlPatternManager().readPatternFile(feedWhiteList).containsUrl(feedUrl);
		}
		return true; // no white listing configured.
	}

	private Response getInternalServerErrorResponse() {
		return Response.status(500).build();
	}

	private Response getForbiddenResponse() {
		return Response.status(403).build();
	}
	
	private Response getBadRequestResponse() {
		return Response.status(400).build();
	}	
}
