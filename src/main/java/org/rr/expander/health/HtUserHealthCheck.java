package org.rr.expander.health;

import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;

import javax.annotation.Nullable;

import com.codahale.metrics.health.HealthCheck;

public class HtUserHealthCheck extends HealthCheck {
	
	@Nullable
	private final String htusers;
	
	public HtUserHealthCheck(String htusers) {
		this.htusers = htusers;
	}

	@Override
	protected Result check() throws Exception {
		if(htusers != null && negate(checkConfigurationFile(htusers))) {
			return Result.unhealthy("A htusers file was configured but did not exists.");
		}

		return Result.healthy();
	}
	
	private boolean checkConfigurationFile(String config) {
		File configFile = new File(config);
		return isBlank(config) || (configFile.isFile() && configFile.canRead());
	}

}
