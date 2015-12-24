package org.rr.expander.feed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.expander.HtUserAuthenticator;
import org.rr.expander.feed.ExpressionParser.SEGMENT_TYPE;
import org.rr.expander.util.HttpInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * This class is responsible to exchange the feed entries description with the
 * selected content from the linked web page.
 */
public class FeedContentExchanger {
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(HtUserAuthenticator.class);
	
	@Nonnull
	private static final String TYPE_ID = "id";

	@Nonnull
	private static final String TYPE_TAG = "tag";

	@Nonnull
	private final String includeExpression;

	public FeedContentExchanger(@Nonnull String includeExpression) {
		this.includeExpression = includeExpression;
	}

	/**
	 * Exchanges the content from each feed entry with the selected part of the linked web page. 
	 * @param list All entries which content should be exchanged.
	 * @throws IOException
	 */
	public void exchangeAll(@Nonnull List<SyndEntry> list) throws IOException {
		list.stream().map(feedEntry -> exchange(feedEntry)).collect(Collectors.toList());
	}

	private @Nullable SyndEntry exchange(@Nullable SyndEntry feedEntry) {
		try {
			@Nonnull String pageContent = loadPageContent(feedEntry);
			@Nonnull String selectedPageContent = mergePageElements(getSelectedPageElements(pageContent, feedEntry.getLink()));
			applyNewContentToEntry(feedEntry, selectedPageContent);
		} catch (IOException e) {
			logger.warn(String.format("Failed to load link '%s'.", feedEntry.getLink()), e);
		}
		return feedEntry;
	}

	private void applyNewContentToEntry(@Nonnull SyndEntry feedEntry, @Nonnull String pageContent) {
		feedEntry.getDescription().setValue(pageContent);
		feedEntry.getDescription().setType("html");
	}

	private @Nonnull String mergePageElements(@Nonnull Collection<Element> elements) {
		StringBuilder resultHtml = new StringBuilder();
		for (Element element : elements) {
			resultHtml.append(element.outerHtml() + "\n");
		}
		return resultHtml.toString();
	}

	private @Nonnull Collection<Element> getSelectedPageElements(@Nonnull String pageContent, @Nonnull String baseUri) {
		Element pageBody = Jsoup.parse(pageContent, baseUri).body();
		List<ExpressionParser> expressionParsers = ExpressionParser.createExpressionParser(includeExpression);
		
		return expressionParsers.stream().map(expressionParser -> selectPageElement(pageBody, expressionParser))
				.filter(selectedElement -> selectedElement != null).collect(Collectors.toList());
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
		if (Objects.equal(SEGMENT_TYPE.ID, expressionParser.getSegmentType(index))) {
			working = selectIdElement(working, expressionParser.getSegmentValue(index));
		} else if (Objects.equal(SEGMENT_TYPE.TAG, expressionParser.getSegmentType(index))) {
			String tagName = expressionParser.getSegmentValue(index);
			if (StringUtils.startsWith(tagName, "*")) {
				working = selectWildcardTagElement(expressionParser, working, index, tagName);
			} else {
				working = selectHierarchicalTagElement(expressionParser, working, index, tagName);
			}
		}
		return working;
	}

	private @Nullable Element selectIdElement(Element working, String elementId) {
		return working.getElementById(elementId);
	}

	private @Nullable Element selectWildcardTagElement(@Nonnull ExpressionParser expressionParser, @Nonnull Element working, 
			int index, @Nonnull String tagName) {
		Elements elements = working.getElementsByTag(StringUtils.substring(tagName, 1));
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
	
	private @Nonnull List<Element> getChildTagsByTagName(@Nonnull Element element, @Nonnull String tagName) {
		List<Element> childTags = new ArrayList<>();
		Elements elementsByTag = element.getElementsByTag(tagName);
		for (Element tagElement : elementsByTag) {
			if(tagElement.parent() == element) {
				childTags.add(tagElement);
			}
		}
		return childTags;
	}

	@VisibleForTesting
	protected @Nonnull String loadPageContent(@Nullable SyndEntry feedEntry) throws IOException {
		String link = feedEntry != null ? feedEntry.getLink() : null;
		if (link != null) {
			return IOUtils.toString(new HttpInputStream(link));
		}
		throw new IllegalArgumentException("link url is empty.");
	}

}
