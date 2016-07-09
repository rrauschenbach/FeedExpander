package org.rr.expander.feed;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.annotation.Nonnull;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PageContentExtractor} is responsible to extract the part of a given web page which
 * is described with a css selector.
 */
public class PageContentExtractor {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(PageContentExtractor.class);
	
	private @Nonnull String cssSelector;
	
	public PageContentExtractor(@Nonnull String includeCssSelector) {
		this.cssSelector = includeCssSelector;
	}
	
	public @Nonnull List<String> extractPageElements(@Nonnull String pageContent, @Nonnull String baseUri) 
			throws IllegalStateException{
		return getSelectedPageElements(pageContent, baseUri).stream()
				.map(element -> element.outerHtml())
				.map(bodyHtml -> cleanHtml(bodyHtml))
				.collect(toList());
	}
	
	private @Nonnull String cleanHtml(@Nonnull String bodyHtml) {
		return Jsoup.clean(bodyHtml, Whitelist.relaxed());
	}
	
	private @Nonnull Elements getSelectedPageElements(@Nonnull String pageContent, @Nonnull String baseUri) 
			throws IllegalStateException {
		return Jsoup.parse(pageContent, baseUri).body().select(cssSelector);
	}

}
