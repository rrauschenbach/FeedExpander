package org.rr.expander.feed;

import static com.google.common.base.Objects.equal;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substring;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.rr.expander.feed.ExpressionParser.SEGMENT_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PageContentExtractor} is responsible to extract the part of a given web page which
 * is described with an expression.
 */
public class PageContentExtractor {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(PageContentExtractor.class);
	
	private @Nonnull String includeExpression;
	
	public PageContentExtractor(@Nonnull String includeExpression) {
		this.includeExpression = includeExpression;
	}
	
	public @Nonnull List<String> extractPageElements(@Nonnull String pageContent, @Nonnull String baseUri) {
		return getSelectedPageElements(pageContent, baseUri).stream()
				.map(element -> element.outerHtml())
				.map(bodyHtml -> cleanHtml(bodyHtml))
				.collect(toList());
	}
	
	private @Nonnull String cleanHtml(@Nonnull String bodyHtml) {
		return Jsoup.clean(bodyHtml, Whitelist.relaxed());
	}
	
	private @Nonnull Collection<Element> getSelectedPageElements(@Nonnull String pageContent, @Nonnull String baseUri) {
		Element pageBody = Jsoup.parse(pageContent, baseUri).body();
		List<ExpressionParser> expressionParsers = ExpressionParser.createExpressionParser(includeExpression);
		
		return expressionParsers.stream()
				.map(expressionParser -> selectPageElement(pageBody, expressionParser))
				.filter(selectedElement -> selectedElement != null)
				.collect(toList());
	}

	private @Nullable Element selectPageElement(@Nonnull Element pageBody, @Nonnull ExpressionParser expressionParser) {
		Element working = pageBody;
		for (int i = 0; i < expressionParser.segmentCount() && working != null; i++) {
			working = selectPageElement(expressionParser, working, i);
		}
		return working;
	}

	private @Nullable Element selectPageElement(@Nonnull ExpressionParser expressionParser, @Nonnull Element working,
			int index) {
		if (equal(SEGMENT_TYPE.ID, expressionParser.getSegmentType(index))) {
			working = selectIdElement(working, expressionParser.getSegmentValue(index));
		} else if (equal(SEGMENT_TYPE.TAG, expressionParser.getSegmentType(index))) {
			String tagName = expressionParser.getSegmentValue(index);
			if (isWildcardSelection(tagName)) {
				working = selectWildcardTagElement(expressionParser, working, index, tagName);
			} else {
				working = selectHierarchicalTagElement(expressionParser, working, index, tagName);
			}
		}
		return working;
	}

	private boolean isWildcardSelection(String tagName) {
		return startsWith(tagName, "*");
	}

	private @Nullable Element selectIdElement(Element working, String elementId) {
		return working.getElementById(elementId);
	}

	private @Nullable Element selectWildcardTagElement(@Nonnull ExpressionParser expressionParser, @Nonnull Element working, 
			int index, @Nonnull String tagName) {
		Elements elements = working.getElementsByTag(substring(tagName, 1));
		return selectTagElement(expressionParser, working, index, tagName, elements);
	}
	
	private @Nullable Element selectHierarchicalTagElement(@Nonnull ExpressionParser expressionParser, @Nonnull Element working, int index, 
			@Nonnull String tagName) {
		List<Element> childTags = getChildTagsByTagName(working, tagName);
		return selectTagElement(expressionParser, working, index, tagName, childTags);
	}

	private @Nullable Element selectTagElement(@Nonnull ExpressionParser expressionParser, @Nonnull Element working, int index, @Nonnull String tagName,
			@Nonnull List<Element> childTags) {
		int tagNumber = expressionParser.getSegmentNumber(index) - 1;
		if(childTags.size() > tagNumber) {
			working = childTags.get(tagNumber);
		} else {
			logger.info(String.format("Trying to get tag '%s' at %s from '%s' failed.", tagName, tagNumber, expressionParser));
			return null;
		}
		return working;
	}
	
	/**
	 * Get all elements which matches to the given <code>tagName</code> and were direct child's of the
	 * given <code>parentElement</code>.
	 * 
	 * @param parentElement The element which child's with the given <code>tagName</code> should be
	 *        returned.
	 * @param tagName The name of the tag.
	 * @return All desired child elements matching to the given <code>tagName</code>.
	 */
	private @Nonnull List<Element> getChildTagsByTagName(@Nonnull Element parentElement, @Nonnull String tagName) {
		return parentElement.getElementsByTag(tagName).stream()
			.filter(tagElement -> tagElement.parent() == parentElement)
			.collect(toList());
	}
}
