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
import org.rr.expander.util.HttpInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndEntry;

class ContentFilter {
	
	@Nonnull
	final static Logger logger = LoggerFactory.getLogger(HtUserAuthenticator.class);

	@Nonnull
	private final String includeExpression;

	public ContentFilter(@Nonnull String includeExpression) {
		this.includeExpression = includeExpression;
	}

	public void apply(@Nonnull List<SyndEntry> list) throws IOException {
		list.stream().map(feedEntry -> apply(feedEntry)).collect(Collectors.toList());
	}

	private SyndEntry apply(SyndEntry feedEntry) {
		String pageContent;
		try {
			pageContent = loadPageContent(feedEntry);
			String html = mergeElements(getSelectedElements(pageContent, feedEntry.getLink()));
			applyToEntry(feedEntry, html);
		} catch (IOException e) {
			logger.warn(String.format("Failed to load link '%s'.", feedEntry.getLink()), e);
		}
		return feedEntry;
	}

	private void applyToEntry(SyndEntry feedEntry, String html) {
		feedEntry.getDescription().setValue(html);
		feedEntry.getDescription().setType("html");
	}

	private @Nonnull String mergeElements(@Nonnull Collection<Element> elements) {
		StringBuilder resultHtml = new StringBuilder();
		for (Element element : elements) {
			resultHtml.append(element.outerHtml() + "\n");
		}
		return resultHtml.toString();
	}

	private @Nonnull Collection<Element> getSelectedElements(@Nonnull String pageContent, @Nonnull String baseUri) {
		Element pageBody = Jsoup.parse(pageContent, baseUri).body();
		List<ExpressionParser> expressionParsers = ExpressionParser.createExpressionParser(includeExpression);
		
		Collection<Element> result = new ArrayList<Element>();
		for (ExpressionParser expressionParser : expressionParsers) {
			Element selectedElement = selectElement(pageBody, expressionParser);
			if (selectedElement != null) {
				result.add(selectedElement);
			}
		}
		return result;
	}

	private @Nullable Element selectElement(@Nonnull Element pageBody, @Nonnull ExpressionParser expressionParser) {
		Element working = pageBody;
		for (int i = 0; i < expressionParser.segmentCount() && working != null; i++) {
			@Nullable String elementId = expressionParser.getIdSegmentValueAt(i);
			@Nullable String tagName = expressionParser.getTagSegmentValueAt(i);
			if (StringUtils.isNotBlank(elementId)) {
				working = selectIdElement(working, elementId);
			} else if (StringUtils.isNotBlank(tagName) && tagName.startsWith("*")) {
				working = selectWildcardTagElement(expressionParser, working, i, tagName);
			} else if (StringUtils.isNotBlank(tagName)) {
				working = selectHierarchicalTagElement(expressionParser, working, i, tagName);
			}
		}
		return working;
	}

	private @Nullable Element selectIdElement(Element working, String elementId) {
		return working.getElementById(elementId);
	}

	private @Nullable Element selectWildcardTagElement(@Nonnull ExpressionParser expressionParser, @Nonnull Element working, int index, 
			@Nonnull String tagName) {
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
		int tagIndexAt = expressionParser.getSegmentIndexAt(index) - 1;
		if(childTags.size() > tagIndexAt) {
			working = childTags.get(tagIndexAt);
		} else {
			logger.info(String.format("Trying to get tag '%s' at %s from '%s' failed.", tagName, tagIndexAt, expressionParser));
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

	private @Nonnull String loadPageContent(@Nullable SyndEntry feedEntry) throws IOException {
		String link = feedEntry != null ? feedEntry.getLink() : null;
		if (link != null) {
			return IOUtils.toString(new HttpInputStream(link));
		}
		throw new IllegalArgumentException("link url is empty.");
	}

}
