package org.rr.expander;


import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.auth.BasicUserPrincipal;
import org.rr.expander.health.ConfigurationHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class ExpanderApplication extends Application<ExpanderConfiguration> {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(ExpanderApplication.class);

	public static void main(String[] args) throws Exception {
		new ExpanderApplication().run(args);
	}
	
	@Override
	public void initialize(Bootstrap<ExpanderConfiguration> bootstrap) {
		bootstrap.addBundle(new ViewBundle<ExpanderConfiguration>());
	}

	@Override
	public void run(ExpanderConfiguration config, Environment environment) throws ClassNotFoundException {
		registerExpanderResource(environment, config.getFeedWhiteList());
		registerUrlCreatorResource(config, environment);
		registerBasicAuth(environment, config.getHtusers());
		registerConfigurationHealthCheck(config, environment);
	}
	
	private String evaluateHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Failed to evaluate host", e);
		}
		return null;
	}
	
	private @Nonnull String getProtocol(ExpanderConfiguration config) {
		return HttpsConnectorFactory.class.isAssignableFrom(getConnectorFactoy(config.getServerFactory()).getClass())
				? "https" : "http";
	}
	
	private int getPort(ExpanderConfiguration config) {
		return getConnectorFactoy(config.getServerFactory()).getPort();
	}

	private @Nullable String getBindHost(ExpanderConfiguration config) {
		return getConnectorFactoy(config.getServerFactory()).getBindHost();
	}
	
	private @Nonnull HttpConnectorFactory getConnectorFactoy(ServerFactory serverFactory) {
		if(serverFactory instanceof DefaultServerFactory) {
			return getDefaultServerFactory(serverFactory);
		} else if(serverFactory instanceof SimpleServerFactory) {
			return getSimpleServerFactory(serverFactory);
		}
		throw new IllegalArgumentException(
				String.format("Unknonw ServerFactory instance '%s'", serverFactory.getClass().getName()));
	}

	private @Nonnull HttpConnectorFactory getSimpleServerFactory(ServerFactory serverFactory) {
		HttpConnectorFactory connector = (HttpConnectorFactory) ((SimpleServerFactory)serverFactory).getConnector();
		if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
		    return connector;
		}
		throw new IllegalArgumentException(String.format("Failed to find any server ConnectorFactory in serverFactory '%s'",
				serverFactory.getClass().getName()));		
	}

	private @Nonnull HttpConnectorFactory getDefaultServerFactory(ServerFactory serverFactory) {
		for (ConnectorFactory connector : ((DefaultServerFactory)serverFactory).getApplicationConnectors()) {
			if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
				return (HttpConnectorFactory) connector;
			}
		}
		throw new IllegalArgumentException(String.format("Failed to find any server ConnectorFactory in serverFactory '%s'",
				serverFactory.getClass().getName()));
	}

	private void registerConfigurationHealthCheck(ExpanderConfiguration config, Environment environment) {
		final ConfigurationHealthCheck healthCheck =
        new ConfigurationHealthCheck(config.getHtusers(), config.getFeedWhiteList());
    environment.healthChecks().register("configuration", healthCheck);
	}

	private void registerExpanderResource(Environment environment, String feedWhiteList) {
		ExpanderResource resource = new ExpanderResource(feedWhiteList);
		environment.jersey().register(resource);
	}
	
	private void registerUrlCreatorResource(ExpanderConfiguration config, Environment environment) {
		String serverUrl = Optional.ofNullable(Optional.ofNullable(getBindHost(config)).orElse(evaluateHostName()))
			.map(host -> getProtocol(config) + "://" + host + ":" + getPort(config) + "/expand")
			.orElse(EMPTY);
		
		ExpanderUrlCreatorResource resource = new ExpanderUrlCreatorResource(serverUrl);
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
