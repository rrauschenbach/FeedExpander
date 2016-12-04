package org.rr.expander.feed;

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
}
