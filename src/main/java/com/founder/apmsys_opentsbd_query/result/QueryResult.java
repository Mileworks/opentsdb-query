package com.founder.apmsys_opentsbd_query.result;

import java.util.List;
import java.util.Map;


public class QueryResult {
	@SuppressWarnings("rawtypes")
	private Map attributes;
	private Long from;
	private Long to;
	private List<QueryResultSeries> series;
	private String query;
	private String status;
	
	@SuppressWarnings("rawtypes")
	public Map getAttributes() {
		return attributes;
	}
	@SuppressWarnings("rawtypes")
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
	public Long getFrom() {
		return from;
	}
	public void setFrom(Long from) {
		this.from = from;
	}
	public Long getTo() {
		return to;
	}
	public void setTo(Long to) {
		this.to = to;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<QueryResultSeries> getSeries() {
		return series;
	}
	public void setSeries(List<QueryResultSeries> series) {
		this.series = series;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
}
