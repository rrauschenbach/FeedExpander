package org.rr.expander.feed;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * This class contains Html related utility methods. 
 */
public class HtmlUtils {

	public static @Nonnull String cleanHtml(@Nonnull String bodyHtml) {
		return Jsoup.clean(bodyHtml, Whitelist.relaxed());
	}

	public static @Nonnull String stripHtml(@Nonnull String html) {
		return Jsoup.parse(html).text();
	}

	/**
	 * Converts the given link to an absolut link if it is not already absolute.
	 * 
	 * @param link The link which is to be converted into an absolut link.
	 * @param pageUrl The page url which is the base to turn a relative to an absolute link.
	 * @return The absolute link.
	 * @throws IllegalArgumentException if the given link or page url is not a valid url. 
	 */
	public static @Nonnull String makeAbsolute(@Nonnull String link, @Nonnull String pageUrl) {
		try {
			URL baseUrl = new URL(pageUrl);
			URL url = new URL(baseUrl, link);
			return url.toString();
		} catch(MalformedURLException e) {
			throw new IllegalArgumentException(String.format("Invalid link '%s' or page url '%s'.", link, pageUrl), e);
		}
	}
}
