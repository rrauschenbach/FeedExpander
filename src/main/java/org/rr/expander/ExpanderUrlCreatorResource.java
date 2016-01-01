package org.rr.expander;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The {@link ExpanderUrlCreatorResource} is the registered resource for the html page which makes
 * it easier to create a FeedExpander url.
 */
@Path("/create")
@Produces(MediaType.TEXT_HTML)
public class ExpanderUrlCreatorResource {
	
	@Nonnull 
	private String serverUrl;
	
	public ExpanderUrlCreatorResource(@Nonnull String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@PermitAll
	@POST
  public ExpanderUrlCreatorView getFeedUrl(
  		@FormParam("feedUrl") String feedUrl,
  		@FormParam("includeExpression") String includeExpression) {
      return new ExpanderUrlCreatorView(serverUrl, feedUrl, includeExpression);
  }
	
	@PermitAll
	@GET
  public ExpanderUrlCreatorView getIntro() {
		return new ExpanderUrlCreatorView(serverUrl, null, null);
	}
  		
}
