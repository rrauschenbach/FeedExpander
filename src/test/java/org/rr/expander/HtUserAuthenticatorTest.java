package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;

public class HtUserAuthenticatorTest {

	@Test
	public void testSimpleSuccessfulAuth() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("kalle", "kanns")).orElse(null));
	}
	
	@Test
	public void testSuccessfulAuthWithMultipleColons() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("peter", "sec:ure")).orElse(null));
	}
	
	@Test
	public void testSuccessfulAuthWithMultipleColonsAtBegin() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("fred", ":secure")).orElse(null));
	}
	
	@Test
	public void testSuccessfulAuthWithMultipleColonsAtBeginAndEnd() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("clara", ":secure:")).orElse(null));
	}
	
	@Test
	public void testSuccessfulAuthWithHash() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("gerda", "#pass")).orElse(null));
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithWrongPass() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("kalle", "wronk")).orElse(null));
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithUnknownUser() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("rumpelstilzchen", "unknown")).orElse(null));
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithEmptyUser() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials(EMPTY, "unknown")).orElse(null));
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithEmptyPass() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("rumpelstilzchen", EMPTY)).orElse(null));
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithEmptyDefinedPass() throws AuthenticationException {
		// an empty pass can be defined but should never be accepted for authentication.
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("morgana", EMPTY)).orElse(null));
	}
	
	private HtUserAuthenticator createHtUserAuthenticator() {
		return new HtUserAuthenticator(EMPTY) {

			@Override
			public HtUserAuthenticator readHtUsers() {
				// no file io here
				return this;
			}}.setHtUsers(createHtUsers());
	}
	
	private List<String> createHtUsers() {
		return new ArrayList<String>() {{
			add("# a comment line");
			add("kalle:kanns");
			add("peter:sec:ure");
			add("fred::secure");
			add("clara::secure:");
			add("gerda:#pass");
			add("morgana:");
		}};
	}
}
