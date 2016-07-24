package org.rr.expander;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.rr.expander.feed.FeedBuilderFactory;
import org.rr.expander.feed.FeedBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.syndication.io.FeedException;

@Path("/expand")
public class ExpanderResource {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(ExpanderResource.class);

	@Nonnull
	@Inject(optional = false)
	private FeedSitesManager feedSitesManager;
	
	@Nonnull
	@Inject(optional = false)
	private FeedBuilderFactory feedBuilderFactory;
	
	@PermitAll
	@GET
	public Response expand(@QueryParam("alias") Optional<String> alias) {
		if(alias.isPresent()) {
			return expandByAlias(alias);
		}
		return getBadRequestResponse();
	}
	
	@Nonnull
	private Response expandByAlias(@Nonnull Optional<String> alias) {
		return alias.transform(new Function<String, Response>() {
			@Override
			public Response apply(@Nonnull String alias) {
				try {
					if(feedSitesManager.containsAlias(alias)) {
						FeedBuilderImpl feedHandler = createFeedHandlerForAlias(alias);
						return getSuccessResponse(feedHandler);
					} 
					logger.warn(String.format("Fetching feed for alias '%s' is not allowed.", alias));
					return getForbiddenResponse();
				} catch (Exception e) {
					logger.warn(String.format("Fetching feed for alias '%s' has failed.", alias), e);
					return getInternalServerErrorResponse();
				}
			}
		}).or(getBadRequestResponse()); // (no alias)
	}

	@Nonnull
	private FeedBuilderImpl createFeedHandler(@Nonnull Optional<Integer> limit, @Nonnull Optional<String> include,
			@Nonnull String feedUrl) throws MalformedURLException, FeedException, IOException {
		FeedBuilderImpl feedHandler = feedBuilderFactory.createFeedBuilder(feedUrl)
				.loadFeed()
				.setLimit(limit.orNull())
				.expand(include.or("*"));
		return feedHandler;
	}

	@Nonnull
	private FeedBuilderImpl createFeedHandlerForAlias(@Nonnull String alias)
			throws MalformedURLException, FeedException, IOException {
		FeedBuilderImpl feedHandler = feedBuilderFactory.createFeedBuilder(feedSitesManager.getFeedUrl(alias))
				.loadFeed()
				.setLimit(feedSitesManager.getLimit(alias))
				.expand(feedSitesManager.getSelector(alias));
		return feedHandler;
	}
	
	@Nonnull
	private Response getSuccessResponse(FeedBuilderImpl feedHandler) throws FeedException {
		return Response.ok(feedHandler.build(), feedHandler.getMediaType()).build();
	}

	@Nonnull
	private Response getInternalServerErrorResponse() {
		return Response.status(500).build();
	}

	@Nonnull
	private Response getForbiddenResponse() {
		return Response.status(403).build();
	}
	
	@Nonnull
	private Response getBadRequestResponse() {
		return Response.status(400).build();
	}	
}
