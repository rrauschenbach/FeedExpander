package org.rr.expander;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class ExpanderConfiguration extends Configuration {
	
	private String htUsers;
	
	private String feedWhiteList;

	private String pageCacheType;
	
	private int pageCacheSize;
	
	@JsonProperty
  public String getHtusers() {
      return htUsers;
  }

  @JsonProperty
  public void setHtusers(String htUsers) {
      this.htUsers = htUsers;
  }
  
  @JsonProperty
	public String getFeedWhiteList() {
		return feedWhiteList;
	}

  @JsonProperty
	public void setFeedWhiteList(String feedWhiteList) {
		this.feedWhiteList = feedWhiteList;
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
	public void setPageCacheSize(int pageCacheSize) {
		this.pageCacheSize = pageCacheSize;
	}

  @JsonProperty
	public int getPageCacheSize() {
		return pageCacheSize;
	}  
}
