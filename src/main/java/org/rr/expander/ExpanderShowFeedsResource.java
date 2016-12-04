package org.rr.expander;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * The {@link ExpanderShowFeedsResource} is the registered resource for the html page which shows all available feeds. 
 */
@Path("/feeds")
@Produces(MediaType.TEXT_HTML)
public class ExpanderShowFeedsResource {
	
	@Nonnull
	@Inject(optional = false)
	@Named("ExpandServiceUrl")
	private String serviceUrl;

	@Nonnull
	@Inject(optional = false)
	private ExpanderFeedSitesManager feedSitesManager;
	
	@PermitAll
	@GET
  public ExpanderShowFeedsView getIntro() {
		return new ExpanderShowFeedsView(serviceUrl, feedSitesManager);
	}
  		
}
