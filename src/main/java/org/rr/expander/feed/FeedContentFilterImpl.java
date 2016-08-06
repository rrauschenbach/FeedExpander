package org.rr.expander.feed;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

public class FeedContentFilterImpl implements FeedContentFilter {
	
	private Pattern regex;

	@Inject
	public FeedContentFilterImpl(@Assisted @Nonnull String regex) {
		this.regex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	public @Nonnull List<SyndEntry> filterInclude(@Nullable List<SyndEntry> feedEntries) {
		if(isNotBlank(regex.pattern())) {
			return feedEntries.stream().filter(entry -> negate(match(entry))).collect(toList());
		}
		return feedEntries;
	}

	@Override
	public @Nonnull List<SyndEntry> filterExclude(@Nullable List<SyndEntry> feedEntries) {
		if(isNotBlank(regex.pattern())) {
			return feedEntries.stream().filter(entry -> match(entry)).collect(toList());
		}
		return feedEntries;
	}
	
	private boolean match(@Nonnull SyndEntry entry) {
		return matchDescription(entry) || matchContent(entry) || matchAuthor(entry) || matchCategory(entry);
	}
	
	@SuppressWarnings("unchecked")
	private boolean matchContent(@Nonnull SyndEntry entry) {
		return ((List<SyndContent>)entry.getContents()).stream()
				.filter(e -> e != null && isNotBlank(e.getValue()))
				.anyMatch(e -> match(e.getValue()));
	}

	private boolean matchDescription(@Nonnull SyndEntry entry) {
		return match(entry.getDescription() != null ? entry.getDescription().getValue() : null);
	}
	
	private boolean matchAuthor(@Nonnull SyndEntry entry) {
		return match(entry.getAuthor() != null ? entry.getAuthor() : null);
	}
	
	@SuppressWarnings("unchecked")
	private boolean matchCategory(@Nonnull SyndEntry entry) {
		return ((List<SyndCategory>)entry.getCategories()).stream()
				.filter(e -> e != null && isNotBlank(e.getName()))
				.anyMatch(e -> match(e.getName()));
	}
	
	private boolean match(@Nullable String text) {
		return Optional.ofNullable(text).map(t -> regex.matcher(t).find()).orElse(Boolean.FALSE);
	}

}
