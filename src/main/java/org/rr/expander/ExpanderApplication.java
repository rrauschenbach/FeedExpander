package org.rr.expander;


import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.http.auth.BasicUserPrincipal;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;

public class ExpanderApplication extends Application<ExpanderConfiguration> {

	public static void main(String[] args) throws Exception {
		new ExpanderApplication().run(args);
	}

	@Override
	public void run(ExpanderConfiguration config, Environment environment) throws ClassNotFoundException {
		registerExpanderResource(environment, config.getFeedWhiteList());
		registerBasicAuth(environment, config.getHtusers());
	}

	private void registerExpanderResource(Environment environment, String feedWhiteList) {
		ExpanderResource resource = new ExpanderResource(feedWhiteList);
		environment.jersey().register(resource);
	}
	
	private void registerBasicAuth(Environment environment, String htusers) {
		if(isNotBlank(htusers)) {
			environment.jersey().register(new AuthDynamicFeature(
	        new BasicCredentialAuthFilter.Builder<BasicUserPrincipal>()
	            .setAuthenticator(new HtUserAuthenticator(htusers))
	            .setRealm("All")
	            .buildAuthFilter()));
		}
	}
	
}
