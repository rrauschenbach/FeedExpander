package org.rr.expander;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.rr.expander.feed.FeedCreator;
import org.rr.expander.feed.FeedCreatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.syndication.io.FeedException;

@Path("/create")
public class CreatorResource {

	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(ExpanderResource.class);

	@Nonnull
	@Inject(optional = false)
	private CreatorPageSitesManager creatorSitesManager;
	
	@Nonnull
	@Inject(optional = false)
	private FeedCreatorFactory feedCreatorFactory;
	
	@PermitAll
	@GET
	public Response expand(@QueryParam("alias") Optional<String> alias) {
		if(alias.isPresent()) {
			return extractByAlias(alias);
		}
		return getBadRequestResponse();
	}
	
	@Nonnull
	private Response extractByAlias(@Nonnull Optional<String> alias) {
		return alias.transform(new Function<String, Response>() {
			@Override
			public Response apply(@Nullable String alias) {
				try {
					if(alias != null && creatorSitesManager.containsAlias(alias)) {
						FeedCreator pageHandlerForAlias = createFeedHandlerForAlias(alias);
						return getSuccessResponse(pageHandlerForAlias);
					}
					return getForbiddenResponse();
				} catch (Exception e) {
					logger.warn(String.format("Fetching feed for alias '%s' has failed.", alias), e);
					return getInternalServerErrorResponse();
				}
			}
		}).or(getBadRequestResponse()); // (no alias)
	}
	
	@Nonnull
	private FeedCreator createFeedHandlerForAlias(@Nonnull String alias)
			throws MalformedURLException, FeedException, IOException {
		FeedCreator feedHandler = feedCreatorFactory
				.createFeedBuilder(creatorSitesManager.getPageUrl(alias))
				.createFeed(creatorSitesManager.getItemSelector(alias), creatorSitesManager.getTitleSelector(alias),
						creatorSitesManager.getLinkSelector(alias), creatorSitesManager.getAuthorSelector(alias));
		return feedHandler;
	}
	
	@Nonnull
	private Response getSuccessResponse(FeedCreator feedCreator) throws FeedException {
		return Response.ok(feedCreator.build(), feedCreator.getMediaType())
				.header("X-Robots-Tag", "noindex, nofollow")
				.build();
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
