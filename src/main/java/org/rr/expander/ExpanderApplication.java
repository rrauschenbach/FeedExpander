package org.rr.expander;


import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.auth.BasicUserPrincipal;
import org.rr.expander.cache.PageCache;
import org.rr.expander.cache.PageCacheFactory;
import org.rr.expander.cache.PageCacheFactory.CACHE_TYPE;
import org.rr.expander.feed.FeedBuilder;
import org.rr.expander.feed.FeedBuilderFactory;
import org.rr.expander.feed.FeedBuilderImpl;
import org.rr.expander.feed.FeedContentExchanger;
import org.rr.expander.feed.FeedContentExchangerFactory;
import org.rr.expander.feed.FeedContentExchangerImpl;
import org.rr.expander.feed.FeedContentFilter;
import org.rr.expander.feed.FeedContentFilterFactory;
import org.rr.expander.feed.FeedContentFilterImpl;
import org.rr.expander.health.HtUserHealthCheck;
import org.rr.expander.loader.UrlLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

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
		Injector injector = createInjector(config);
		registerExpanderResource(environment, injector);
		registerShowFeedsResource(environment, injector);
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
		final HtUserHealthCheck healthCheck =
        new HtUserHealthCheck(config.getHtusers());
    environment.healthChecks().register("htuser", healthCheck);
	}

	private void registerExpanderResource(Environment environment, Injector injector) {
		environment.jersey().register(injector.getInstance(ExpanderResource.class));
	}
	
	private void registerShowFeedsResource(Environment environment, Injector injector) {
		environment.jersey().register(injector.getInstance(ExpanderShowFeedsResource.class));
	}
	
	private Injector createInjector(ExpanderConfiguration config) {
    return Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
        	bindFeedSitesManager(config);
        	bindExpandServiceUrl(config);
        	bindUrlLoaderFactory();
        	bindPageCache(config);
        	bindFeedBuilder();
        	bindFeedContentExchanger();
        	bindFeedContentFilter();
        }

				private void bindFeedContentExchanger() {
					install(new FactoryModuleBuilder()
       	     .implement(FeedContentExchanger.class, FeedContentExchangerImpl.class)
       	     .build(FeedContentExchangerFactory.class));
				}

				private void bindFeedContentFilter() {
					install(new FactoryModuleBuilder()
       	     .implement(FeedContentFilter.class, FeedContentFilterImpl.class)
       	     .build(FeedContentFilterFactory.class));
				}
				
				private void bindFeedBuilder() {
					install(new FactoryModuleBuilder()
        	     .implement(FeedBuilder.class, FeedBuilderImpl.class)
        	     .build(FeedBuilderFactory.class));
				}

				private void bindPageCache(ExpanderConfiguration config) {
					bind(PageCache.class).toInstance(PageCacheFactory.createPageCacheFactory(
        			CACHE_TYPE.valueOf(config.getPageCacheType()), config.getPageCacheSize(), config.getPageCachePath()).getPageCache());
				}

				private void bindUrlLoaderFactory() {
					bind(UrlLoaderFactory.class).toInstance(UrlLoaderFactory.createURLLoaderFactory());
				}

				private void bindExpandServiceUrl(ExpanderConfiguration config) {
					bind(String.class).annotatedWith(Names.named("ExpandServiceUrl")).toInstance(getExpandServiceUrl(config));
				}

				private void bindFeedSitesManager(ExpanderConfiguration config) {
					bind(FeedSitesManager.class).toInstance(new FeedSitesManager(config.getFeedSites()));
				}
    });
	}
	
	private String getExpandServiceUrl(ExpanderConfiguration config) {
		String serverUrl = Optional.ofNullable(Optional.ofNullable(getBindHost(config)).orElse(evaluateHostName()))
			.map(host -> getExpandServiceUrl(config, host))
			.orElse(EMPTY);
		return serverUrl;
	}

	private String getExpandServiceUrl(ExpanderConfiguration config, String host) {
		return getProtocol(config) + "://" + host + ":" + getPort(config);
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
