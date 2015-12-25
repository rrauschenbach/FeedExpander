package org.rr.expander;

import javax.annotation.Nonnull;
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

	@PermitAll
	@GET
	public Response expand(
			final @QueryParam("feedUrl") Optional<String> feedUrl,
			final @QueryParam("include") Optional<String> include) {
		return feedUrl.transform(new Function<String, Response>() {
			public Response apply(String feedUrl) {
				try {
					if (feedUrl != null ) {
						FeedBuilder feedHandler = new FeedBuilder(feedUrl).loadFeed().expand(include.or("*"));
						return Response.ok(feedHandler.build(), feedHandler.getMimeType()).build();
					}
				} catch (Exception e) {
					logger.warn(String.format("Fetching feed for '%s' has failed", feedUrl), e);
				}
				return Response.status(500).build();
			}
		}).or(Response.status(404).build());
	}
}
