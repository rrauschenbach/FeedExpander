package org.rr.expander;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.rr.expander.FeedSitesManager.Entries.Entry;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;

/**
 * Manager which is able to parse and provide values from the configuration file specified with the constructor. 
 */
public class FeedSitesManager {
	
	static class Entries {
		
		static class Entry {
			
			static class Filter {
				@JsonProperty("include")
				private String include;
				@JsonProperty("exclude")
				private String exclude;
				
				public String getInclude() {
					return include;
				}
				public String getExclude() {
					return exclude;
				}
			}
			
			@JsonProperty("alias")
			private String alias;
			@JsonProperty("description")
			private String description;
			@JsonProperty("feedUrl")
			private String feedUrl;
			@JsonProperty("selector")
			private String selector;
			@JsonProperty("limit")
			private int limit;
			@JsonProperty("filter")
			private List<Filter> filter;
			
			public String getDescription() {
				return description;
			}

			public String getFeedUrl() {
				return feedUrl;
			}

			public String getSelector() {
				return selector;
			}

			public int getLimit() {
				return limit;
			}

			public String getAlias() {
				return alias;
			}

			public Optional<List<Filter>> getFilter() {
				return Optional.ofNullable(filter);
			}
		}
		
    @JsonProperty("feeds")
    private List<Entry> entries;

    public List<Entry> getEntries() {
        return entries;
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
	public String getDescription(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getDescription();
	}
	
	@Nullable
	public String getFeedUrl(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getFeedUrl();
	}

	@Nullable
	public String getSelector(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getSelector();
	}
	

	@Nonnull
	private List<String> getFilter(@Nullable String alias, @Nonnull Function<? super Entry.Filter, ? extends String> mapper) throws IOException {
		return Optional.ofNullable(getEntries().get(alias))
				.orElse(new Entry())
				.getFilter()
				.orElse(Collections.emptyList())
				.stream()
				.map(mapper)
				.filter(s -> s != null)
				.collect(toList());
	}
	
	@Nonnull
	public List<String> getIncludeFilter(@Nullable String alias) throws IOException {
		return getFilter(alias, new Function<Entry.Filter, String>() {

			@Override
			public String apply(Entry.Filter f) {
				return f.getInclude();
			}
		});
	}

	@Nonnull
	public List<String> getExcludeFilter(@Nullable String alias) throws IOException {
		return getFilter(alias, new Function<Entry.Filter, String>() {

			@Override
			public String apply(Entry.Filter f) {
				return f.getExclude();
			}
		});
	}
	
	@Nullable
	public Integer getLimit(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getLimit();
	}	
	
	@Nonnull
	public Set<String> getAliases() throws IOException {
		return getEntries().keySet();
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
			
			return new ObjectMapper(new YAMLFactory()).readValue(readFeedSitesConfig(feedSitesFile), Entries.class).getEntries().stream()
				.collect(toMap(entry -> entry.getAlias(), entry -> entry));
	}
	
	@VisibleForTesting
	protected String readFeedSitesConfig(@Nonnull Path feedSitesFile) throws IOException {
		 return FileUtils.readFileToString(feedSitesFile.toFile());
	}

}
