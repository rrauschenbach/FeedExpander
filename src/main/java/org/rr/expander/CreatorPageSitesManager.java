package org.rr.expander;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.rr.expander.CreatorPageSitesManager.Entries.Entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;
/**
 * Manager which is able to parse and provide values from the configuration file specified with the constructor. 
 */
public class CreatorPageSitesManager {
	
	static class Entries {
		
		static class Entry {
			
			@JsonProperty("alias")
			private String alias;
			@JsonProperty("description")
			private String description;
			@JsonProperty("title")
			private String title;
			@JsonProperty("pageUrl")
			private String pageUrl;
			@JsonProperty("item-selector")
			private String itemSelector;
			@JsonProperty("title-selector")
			private String titleSelector;
			@JsonProperty("link-selector")
			private String linkSelector;
			@JsonProperty("author-selector")
			private String authorSelector;
			
			public String getDescription() {
				return description;
			}
			
			public String getTitle() {
				return title;
			}

			public String getPageUrl() {
				return pageUrl;
			}
			
			public String getAuthorSelector() {
				return authorSelector;
			}

			public String getItemSelector() {
				return itemSelector;
			}
			
			public String getTitleSelector() {
				return titleSelector;
			}
			
			public String getLinkSelector() {
				return linkSelector;
			}
			
			public String getAlias() {
				return alias;
			}
		}
		
    @JsonProperty("pages")
    private List<Entry> entries;

    public List<Entry> getEntries() {
        return entries;
    }
	}
	
	/** the feed sites config file name. */
	private Path pageSitesFile;
	
	/** stores the modified time stamp of the time when the config file was read the last time. */
	private long feedSitesFileModified;
	
	/** the feed site configuration will be stored here. */
	private Map<String, Entry> feedSiteEntries;

	public CreatorPageSitesManager(@Nullable String pageSitesFile) {
		this.pageSitesFile = Paths.get(pageSitesFile);
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
	public String getTitle(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getTitle();
	}
	
	@Nullable
	public String getPageUrl(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getPageUrl();
	}
	
	@Nullable
	public String getAuthor(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getPageUrl();
	}

	@Nullable
	public String getItemSelector(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getItemSelector();
	}

	@Nullable
	public String getTitleSelector(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getTitleSelector();
	}
	
	@Nullable
	public String getLinkSelector(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getLinkSelector();
	}
	
	@Nullable
	public String getAuthorSelector(@Nullable String alias) throws IOException {
		return Optional.ofNullable(getEntries().get(alias)).orElse(new Entry()).getAuthorSelector();
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
				feedSitesFileModified < pageSitesFile.toFile().lastModified();
	}
	
	@Nonnull
	private Map<String, Entry> readFeedSitesFile() throws IOException {
			feedSitesFileModified = pageSitesFile.toFile().lastModified();
			
			return new ObjectMapper(new YAMLFactory()).readValue(readFeedSitesConfig(pageSitesFile), Entries.class).getEntries().stream()
				.collect(toMap(entry -> entry.getAlias(), entry -> entry));
	}
	
	@VisibleForTesting
	protected String readFeedSitesConfig(@Nonnull Path feedSitesFile) throws IOException {
		 return FileUtils.readFileToString(feedSitesFile.toFile(), UTF_8);
	}

}
