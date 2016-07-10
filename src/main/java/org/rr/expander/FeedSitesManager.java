package org.rr.expander;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;

/**
 * Manager which is able to parse and provide values from the configuration file specified with the constructor. 
 */
public class FeedSitesManager {
	
	private static class Entry {
		private String description;
		private String feedUrl;
		private String selector;
		private int limit;

		public String getDescription() {
			return description;
		}

		public Entry setDescription(String description) {
			this.description = description;
			return this;
		}

		public String getFeedUrl() {
			return feedUrl;
		}

		public Entry setFeedUrl(String feedUrl) {
			this.feedUrl = feedUrl;
			return this;
		}

		public String getSelector() {
			return selector;
		}

		public Entry setSelector(String selector) {
			this.selector = selector;
			return this;
		}

		public int getLimit() {
			return limit;
		}

		public Entry setLimit(int limit) {
			this.limit = limit;
			return this;
		}
	}
	
	@Nonnull
	private final static Logger logger = LoggerFactory.getLogger(FeedSitesManager.class);
	
	@Nonnull
	private static final String SEPARATOR_CHAR = "|";

	/** the feed sites config file name. */
	private Path feedSitesFile;
	
	/** stores the modified time stamp of the time when the config file was read the last time. */
	private long feedSitesFileModified;
	
	/** the feed site configuration will be stored here. */
	private Map<String, Entry> feedSiteEntries;

	public FeedSitesManager(@Nullable String feedSitesFile) {
		this.feedSitesFile = Paths.get(feedSitesFile);
	}
	
	public boolean containsAlias(@Nullable String alias) throws IOException {
		if(isNotBlank(alias)) {
			return getEntries().containsKey(alias);
		}
		return false;
	}
	
	public int size() throws IOException {
		return getEntries().size();
	}
	
	@Nullable
	public String getDescription(String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getDescription();
	}
	
	@Nullable
	public String getFeedUrl(String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getFeedUrl();
	}

	@Nullable
	public String getSelector(String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getSelector();
	}
	
	@Nullable
	public Integer getLimit(String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getLimit();
	}	
	
	@Nonnull
	private Map<String, Entry>  getEntries() throws IOException {
		if(feedSiteEntries == null || isReReadFeedSitesFileNecessary()) {
			feedSiteEntries = readFeedSitesFile();
		}
		return feedSiteEntries;
	}
	
	private boolean isReReadFeedSitesFileNecessary() {
		return feedSitesFileModified == 0 ||  
				feedSitesFileModified < feedSitesFile.toFile().lastModified();
	}
	
	@Nonnull
	private Map<String, Entry> readFeedSitesFile() throws IOException {
			feedSitesFileModified = feedSitesFile.toFile().lastModified();
			return getFeedSitesStream()
				.filter(line -> isValidConfigurationLine(line))
				.collect(toMap(line -> getAlias(line), line -> createEntry(line)));
	}

	@VisibleForTesting
	@Nonnull
	protected Stream<String> getFeedSitesStream() throws IOException {
		return Files.readAllLines(feedSitesFile, StandardCharsets.UTF_8).stream();
	}
	
	@Nonnull
	private Entry createEntry(String line) throws IllegalArgumentException {
		List<String> parts = Splitter.on(SEPARATOR_CHAR).trimResults().splitToList(line);
		if(parts.size() == 5) {
			return new Entry()
					.setDescription(parts.get(1))
					.setFeedUrl(parts.get(2))
					.setSelector(parts.get(3))
					.setLimit(toInt(parts.get(4), Integer.MAX_VALUE));
		}
		throw new IllegalArgumentException(String.format("The line '%s' is not a valid configuration line.", line));
	}
	
	@Nonnull
	private String getAlias(@Nonnull String line) {
		return StringUtils.substringBefore(line, SEPARATOR_CHAR).trim();
	}

	private boolean isValidConfigurationLine(@Nonnull String line) {
		return !isBlank(line) && !trimToEmpty(line).startsWith("#");
	}

}
