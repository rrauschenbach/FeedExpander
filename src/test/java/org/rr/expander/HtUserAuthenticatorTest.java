package org.rr.expander;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;

public class HtUserAuthenticatorTest {

	@Test
	public void testSimpleSuccessfulAuth() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("kalle", "kanns")).orNull());
	}
	
	@Test
	public void testSuccessfulAuthWithMultipleColons() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("peter", "sec:ure")).orNull());
	}
	
	@Test
	public void testSuccessfulAuthWithMultipleColonsAtBegin() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("fred", ":secure")).orNull());
	}
	
	@Test
	public void testSuccessfulAuthWithMultipleColonsAtBeginAndEnd() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("clara", ":secure:")).orNull());
	}
	
	@Test
	public void testSuccessfulAuthWithHash() throws AuthenticationException {
		assertNotNull(createHtUserAuthenticator().authenticate(new BasicCredentials("gerda", "#pass")).orNull());
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithWrongPass() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("kalle", "wronk")).orNull());
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithUnknownUser() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("rumpelstilzchen", "unknown")).orNull());
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithEmptyUser() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials(EMPTY, "unknown")).orNull());
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithEmptyPass() throws AuthenticationException {
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("rumpelstilzchen", EMPTY)).orNull());
	}
	
	@Test
	public void testSimpleUnSuccessfulAuthWithEmptyDefinedPass() throws AuthenticationException {
		// an empty pass can be defined but should never be accepted for authentication.
		assertNull(createHtUserAuthenticator().authenticate(new BasicCredentials("morgana", EMPTY)).orNull());
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
