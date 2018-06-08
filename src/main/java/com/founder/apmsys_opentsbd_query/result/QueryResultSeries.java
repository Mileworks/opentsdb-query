package com.founder.apmsys_opentsbd_query.result;

import java.util.Map;

public class QueryResultSeries {

	private String displayName;
	private Long end;
	private Integer interval;
	private Long start;
	private Integer queryId;
	private Map<Long, Double> pointlist;
	private Map<String, String> tags;
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Long getEnd() {
		return end;
	}
	public void setEnd(Long end) {
		this.end = end;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	public Long getStart() {
		return start;
	}
	public void setStart(Long start) {
		this.start = start;
	}
	public Integer getQueryId() {
		return queryId;
	}
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
	public Map<Long, Double> getPointlist() {
		return pointlist;
	}
	public void setPointlist(Map<Long, Double> pointlist) {
		this.pointlist = pointlist;
	}
	public Map<String, String> getTags() {
		return tags;
	}
	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}
}
