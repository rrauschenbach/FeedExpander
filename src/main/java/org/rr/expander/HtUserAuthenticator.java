package org.rr.expander;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.auth.BasicUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
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

	private Map<String, String> userPasses;
	
	public HtUserAuthenticator(@Nonnull String htusers) {
		this.htusers = htusers;
	}
	
	public @Nonnull HtUserAuthenticator setHtUsers(@Nonnull List<String> userPasses) {
		this.userPasses = userPasses.stream()
				.filter(line -> isNoValidUserCredentialLine(line))
				.map(line -> splitUserPass(line))
				.collect(getUserPassMapCollector());
		return this;
	}
	
	@VisibleForTesting
	protected @Nonnull HtUserAuthenticator readHtUsers() {
		try {
			setHtUsers(Files.readAllLines(Paths.get(htusers), StandardCharsets.UTF_8).stream().collect(toList()));
		} catch (Exception e) {
			logger.warn(String.format("Loading user file '%s' has failed.", htusers), e);
		}
		return this;
	}
	
	private @Nullable String getPassword(@Nullable String user) {
		return Optional.fromNullable(userPasses).transform(map -> Optional.fromNullable(map.get(user))).get().orNull();
	}

	private @Nonnull Collector<ImmutablePair<String, String>, ?, Map<String, String>> getUserPassMapCollector() {
		return toMap(userAndPass -> userAndPass.left,  userAndPass -> userAndPass.right);
	}

	private @Nonnull ImmutablePair<String, String> splitUserPass(@Nonnull String line) {
		String[] userAndPass = line.split(":", 2);
		return new ImmutablePair<>(userAndPass[0], userAndPass[1]);
	}

	private boolean isNoValidUserCredentialLine(@Nullable String line) {
		return negate(isCommentLine(line)) && isUserPassSchema(line);
	}

	private boolean isUserPassSchema(@Nullable String line) {
		return line.matches(".+:.+");
	}

	private boolean isCommentLine(@Nullable String htUserLine) {
		return trimToEmpty(htUserLine).startsWith("#");
	}
	
	@Override
	public Optional<BasicUserPrincipal> authenticate(BasicCredentials credentials) throws AuthenticationException {
		String loginUserName = credentials.getUsername();
		String loginPassword = credentials.getPassword();
		String htUsersPass = readHtUsers().getPassword(loginUserName);
		if(negate(comparePasswords(loginPassword, htUsersPass))) {
			logger.info("Failed to login " + loginUserName);
			return Optional.absent();
		}
		return Optional.of(new BasicUserPrincipal(loginUserName));
	}

	private boolean comparePasswords(@Nullable String loginPassword, @Nullable String htUsersPass) {
		return isNotBlank(loginPassword) && isNotBlank(htUsersPass) && StringUtils.equals(htUsersPass, loginPassword);
	}
}
