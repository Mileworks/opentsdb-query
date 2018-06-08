package com.founder.apmsys_opentsbd_query.param;

import java.util.Map;

/**
 * 接受最小参数
 *
 *
 * @author mr.liang
 */
public class QueryBatchParam{

	private Long begin;
	private Long end;
	private Integer interval; 		//如果interval为null或者<=0 ，那么不采用 downsample
	private String q;
	@SuppressWarnings("rawtypes")
	private Map attributes;
	
	private String intervalAggregator;//downsample 中的 aggregator
	
	public String getIntervalAggregator() {
		return intervalAggregator;
	}

	public void setIntervalAggregator(String intervalAggregator) {
		this.intervalAggregator = intervalAggregator;
	}

	public Long getBegin() {
		return begin;
	}

	public void setBegin(Long begin) {
		this.begin = begin;
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
	public String getQ() {
		return q;
	}
	public void setQ(String q) {
		this.q = q;
	}
	@SuppressWarnings("rawtypes")
	public Map getAttributes() {
		return attributes;
	}
	@SuppressWarnings("rawtypes")
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
}
