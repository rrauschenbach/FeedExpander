package org.rr.expander;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.BasicUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

/**
 * The {@link HtUserAuthenticator} uses a file containing the user credentials to authenticate a
 * user/pass http auth request.
 */
public class HtUserAuthenticator implements Authenticator<BasicCredentials, BasicUserPrincipal> {
	
	@Nonnull
	final static Logger logger = LoggerFactory.getLogger(HtUserAuthenticator.class);

	@Nonnull
	private String htusers;
	
	public HtUserAuthenticator(@Nonnull String htusers) {
		this.htusers = htusers;
	}
	
	private Map<String, String> readHtUsers() {
		try (BufferedReader in = new BufferedReader(new FileReader(new File(htusers)))) {
			return in.lines()
					.filter(line -> isNoValidUserCredentialLine(line))
					.map(line -> splitUserPass(line))
					.collect(getUserPassMapCollector());
		} catch (Exception e) {
			logger.warn(String.format("Loading user file '%s' has failed.", htusers), e);
		}
		return Collections.emptyMap();
	}

	private Collector<String[], ?, Map<String, String>> getUserPassMapCollector() {
		return toMap(line -> line[0], // user
						     line -> line[1], // pass
						     (name1, name2) -> name1 + ";" + name2);
	}

	private String[] splitUserPass(@NotNull String line) {
		return split(line, ":", 2);
	}

	private boolean isNoValidUserCredentialLine(@Nullable String line) {
		return negate(isCommentLine(line)) && containsSeparatorChar(line);
	}

	private boolean containsSeparatorChar(@Nullable String line) {
		return line.matches(".+:.+");
	}

	private boolean isCommentLine(@Nullable String htUserLine) {
		return trimToEmpty(htUserLine).startsWith("#");
	}
	
	@Override
	public Optional<BasicUserPrincipal> authenticate(BasicCredentials credentials) throws AuthenticationException {
		String loginUserName = credentials.getUsername();
		String loginPassword = credentials.getPassword();
		String htUsersPass = readHtUsers().get(loginUserName);
		if(negate(StringUtils.equals(htUsersPass, loginPassword))) {
			logger.info("Failed to login " + loginUserName);
			return Optional.absent();
		}
		return Optional.of(new BasicUserPrincipal(loginUserName));
	}
}
