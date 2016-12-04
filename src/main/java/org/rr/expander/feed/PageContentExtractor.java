package org.rr.expander.feed;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The {@link PageContentExtractor} is responsible to extract the part of a given web page which
 * is described with a css selector.
 */
public class PageContentExtractor {
	
	private @Nonnull String cssSelector;
	
	private @Nonnull List<String> pageElements = Collections.emptyList();
	
	public PageContentExtractor(@Nonnull String cssSelector) {
		this.cssSelector = cssSelector;
	}
	
	public @Nonnull PageContentExtractor extractPageElements(@Nonnull String pageContent, @Nonnull String baseUri) 
			throws IllegalStateException {
		pageElements = getSelectedPageElements(pageContent, baseUri).stream()
				.map(element -> toHtml(element))
				.collect(toList());
		return this;
	}
	
	public @Nonnull PageContentExtractor cleanHtml() {
		pageElements = pageElements.stream().map(bodyHtml -> HtmlUtils.cleanHtml(bodyHtml)).collect(toList());
		return this;
	}
	
	public @Nonnull List<String> getPageElements() {
		return pageElements;
	}
	
	public @Nonnull String getMergedPageElements() {
		return mergeHtmlElements(pageElements);
	}

	public @Nonnull String getFirstPageElement() {
		return pageElements.isEmpty() ? EMPTY : pageElements.get(0);
	}
	
	public @Nonnull List<String> getGroupedPageElements() throws IllegalArgumentException {
		int groups = StringUtils.countMatches(cssSelector, ',') + 1;
		
		if(pageElements.size() % groups != 0) {
			throw new IllegalArgumentException(String.format("The selector %s selects %s elements but defines %s groups.",
					cssSelector, pageElements.size(), groups));
		}
		
		List<String> result = new ArrayList<>();
		for (int i = 0; i < pageElements.size(); i += groups) {
			result.add(mergeHtmlElements(pageElements.subList(i, i + groups)));
		}
		return result;
	}
	
	private @Nonnull String mergeHtmlElements(@Nonnull Collection<String> htmlElements) {
		return join(htmlElements, "\n");
	}

	private String toHtml(Element element) {
		return makeImageUrlsAbsolute(element).outerHtml();
	}

	private Element makeImageUrlsAbsolute(Element element) {
		element.select("img").spliterator().forEachRemaining(e -> e.attr("src", e.absUrl("src")));
		return element;
	}
	
	private @Nonnull Elements getSelectedPageElements(@Nonnull String pageContent, @Nonnull String baseUri) 
			throws IllegalStateException {
		return Jsoup.parse(pageContent, baseUri).body().select(cssSelector);
	}

}
