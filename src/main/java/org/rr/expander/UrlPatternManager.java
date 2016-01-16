package org.rr.expander;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UrlPatternManager} is responsible to manage (for example) a white list of urls 
 * with wild card support.
 */
public class UrlPatternManager {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(UrlPatternManager.class);
	
	@Nullable
	private List<String> urlPatterns;
	
	/**
	 * Test if the given url matches to one of the url patterns previously loaded
	 * with the {@link #readPatternFile(String)} or {@link #setUrlPatterns(List)} method.
	 * 
	 * @param url The url to be tested.
	 * @return <code>true</code> if the given url was found in the list and <code>false</code>
	 *         otherwise.
	 */
	public boolean containsUrl(@Nonnull String url) {
		if(urlPatterns == null) {
			logger.warn("No url patterns loaded.");
			return false;
		}
		
		String normalizedUrl = nomalizeUrl(url);
		return urlPatterns.stream()
				.filter(line -> matches(line, normalizedUrl))
				.findFirst().isPresent();
	}
	
	private boolean matches(@Nonnull String urlPattern, @Nonnull String url) {
		try {
		return Pattern.compile(urlPattern).matcher(url).matches();
		} catch(Exception e) {
			logger.warn(String.format("Invalid pattern %s", urlPattern), e);
			return false;
		}
	}
	
	@Nonnull
	private String nomalizeUrl(@Nonnull String url) {
		return removeWWWPrefix(removeProtocolPrefix(removeTrailingSlash(trim(url))));
	}
	
	@Nonnull
	private String toRegEx(String pattern) {
		String result = pattern;
		result = replace(result, "/*", "*");  // support both "mytest3.de/path/*" and "mytest3.de/path*"
		result = replace(result, "*.", "*");  // support both "*.mytest3.de" and "*mytest3.de"
		result = replace(result, ".", "\\."); // escape '.' characters
		result = replace(result, "*", ".*");  // use .* for regex wild cards.
		return result;
	}
	
	@Nonnull
	private String removeTrailingSlash(@Nonnull String url) {
		String result = removeEnd(url, "/");
		return result;
	}
	
	@Nonnull
	private String removeProtocolPrefix(@Nonnull String url) {
		return url.replaceAll(".*://", "");
	}
	
	@Nonnull
	private String removeWWWPrefix(@Nonnull String url) {
		return url.replaceAll("^w{3}\\.", "");
	}
	
	private boolean isCommentLine(@Nullable String htUserLine) {
		return trimToEmpty(htUserLine).startsWith("#");
	}
	
	@Nonnull
	public UrlPatternManager setUrlPatterns(@Nonnull List<String> urlPatterns) {
		this.urlPatterns = urlPatterns.stream()
				.filter(line -> negate(isCommentLine(line)) && isNotBlank(line))
				.map(line -> toRegEx(nomalizeUrl(line)))
				.collect(toList());
		return this;
	}
	
	@Nonnull
	public UrlPatternManager readPatternFile(@Nonnull String listFile) {
		try {
			setUrlPatterns(Files.readAllLines(Paths.get(listFile), StandardCharsets.UTF_8).stream().collect(toList()));
		} catch (Exception e) {
			urlPatterns = Collections.emptyList();
			logger.warn(String.format("Loading list file '%s' has failed.", listFile), e);
		}
		return this;
	}	
}
