package com.founder.apmsys_opentsbd_query.test;

public class InfoDto {
	
	private String querySql;
	private Object filterBefore;
	private Object filterAfter;
	private Object httpParam;
	private Object opentsdbResult;
	private Object result;
	private boolean isQueryRedis;
	
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public Object getHttpParam() {
		return httpParam;
	}
	public Object getOpentsdbResult() {
		return opentsdbResult;
	}
	public void setOpentsdbResult(Object opentsdbResult) {
		this.opentsdbResult = opentsdbResult;
	}
	public void setHttpParam(Object httpParam) {
		this.httpParam = httpParam;
	}
	public boolean isQueryRedis() {
		return isQueryRedis;
	}
	public void setQueryRedis(boolean isQueryRedis) {
		this.isQueryRedis = isQueryRedis;
	}
	public Object getFilterBefore() {
		return filterBefore;
	}
	public void setFilterBefore(Object filterBefore) {
		this.filterBefore = filterBefore;
	}
	public Object getFilterAfter() {
		return filterAfter;
	}
	public void setFilterAfter(Object filterAfter) {
		this.filterAfter = filterAfter;
	}
	public String getQuerySql() {
		return querySql;
	}
	public void setQuerySql(String querySql) {
		this.querySql = querySql;
	}
}
