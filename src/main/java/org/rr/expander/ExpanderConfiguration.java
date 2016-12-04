package org.rr.expander;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class ExpanderConfiguration extends Configuration {
	
	private String htUsers;
	
	private String feedSites;
	
	private String pageSites;

	private String pageCacheType;
	
	private String pageCacheConfigurationFileName;
	
	@JsonProperty
  public String getHtusers() {
      return htUsers;
  }

  @JsonProperty
  public void setHtusers(String htUsers) {
      this.htUsers = htUsers;
  }

  @JsonProperty
  public String getFeedSites() {
		return feedSites;
	}
  
  @JsonProperty
  public String getPageSites() {
		return pageSites;
	}

  @JsonProperty
	public void setFeedSites(String feedSites) {
		this.feedSites = feedSites;
	}

	@JsonProperty
  public String getPageCacheType() {
		return pageCacheType;
	}

  @JsonProperty
	public void setPageCacheType(String pageCacheType) {
		this.pageCacheType = pageCacheType;
	}

  @JsonProperty
	public String getPageCacheConfigurationFileName() {
		return pageCacheConfigurationFileName;
	}

  @JsonProperty
	public void setPageCacheConfigurationFileName(String pageCacheConfigurationFileName) {
		this.pageCacheConfigurationFileName = pageCacheConfigurationFileName;
	}

}
