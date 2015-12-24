package org.rr.expander;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.BasicUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class HtUserAuthenticator implements Authenticator<BasicCredentials, BasicUserPrincipal> {
	
	@Nonnull
	final static Logger logger = LoggerFactory.getLogger(HtUserAuthenticator.class);

	private String htusers;
	
	public HtUserAuthenticator(String htusers) {
		this.htusers = htusers;
	}
	
	private Map<String, String> readConfig() {
		try {
			Map<String, String> result = new HashMap<>();
			LineIterator users = FileUtils.lineIterator(new File(htusers));
			while(users.hasNext()) {
				String user = users.next();
				if(!isCommentLine(user)) {
					result.put(StringUtils.substringBefore(user, ":"), StringUtils.substringAfter(user, ":"));
				}
			}
			return result;
		} catch (Exception e) {
			logger.warn(String.format("Loading user file '%s' has failed.", htusers), e);
		}
		return Collections.emptyMap();
	}

	private boolean isCommentLine(@Nonnull String htUserLine) {
		return StringUtils.trimToEmpty(htUserLine).startsWith("#");
	}
	
	@Override
	public Optional<BasicUserPrincipal> authenticate(BasicCredentials credentials) throws AuthenticationException {
		String password = readConfig().get(credentials.getUsername());
		if(!StringUtils.equals(password, credentials.getPassword())) {
			return Optional.absent();
		}

		return Optional.of(new BasicUserPrincipal(credentials.getUsername()));
	}
}
