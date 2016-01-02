package org.rr.expander.health;

import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;

import javax.annotation.Nullable;

import com.codahale.metrics.health.HealthCheck;

public class ConfigurationHealthCheck extends HealthCheck {
	
	@Nullable
	private final String htusers;
	
	@Nullable
	private final String feedWhiteList;

	public ConfigurationHealthCheck(String htusers, String feedWhiteList) {
		this.htusers = htusers;
		this.feedWhiteList = feedWhiteList;
	}

	@Override
	protected Result check() throws Exception {
		if(negate(checkConfigurationFile(htusers))) {
			return Result.unhealthy("A htusers file was configured but did not exists.");
		}
		
		if(negate(checkConfigurationFile(feedWhiteList))) {
			return Result.unhealthy("A feedWhiteList file was configured but did not exists.");
		}
		
		return Result.healthy();
	}
	
	private boolean checkConfigurationFile(String config) {
		File configFile = new File(config);
		return isBlank(config) || (configFile.isFile() && configFile.canRead());
	}

}