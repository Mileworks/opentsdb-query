package com.founder.apmsys_opentsbd_query.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.founder.apmsys_opentsdb.param.Filter;

public class GroupFilter {

	public List<Filter> filters;

	public LinkedHashMap<String, Set<String>> tagMap;

	
	//给userId用的
	public void addFilterAtTypeLiteralOr(String tagK,String tagV) {
		Filter filter = new Filter();
		filter.setFilter(tagV);
		filter.setTagk(tagK);
		filter.setGroupBy(false);
		filter.setType("literal_or");
		
		addFilter(filter);	
	}
	
	public void addFilterAtTypeLiteralOr(String tagK,Set<String> tagVs,boolean groupBy) {
		StringBuilder sbFilter = new StringBuilder();
		for(String tagV : tagVs){
			sbFilter.append(tagV).append("|");
		}
		sbFilter.deleteCharAt(sbFilter.length()-1);
		
		Filter filter = new Filter();
		filter.setFilter(sbFilter.toString());
		filter.setTagk(tagK);
		filter.setGroupBy(groupBy);
		filter.setType("literal_or");
		
		addFilter(filter);	
	}
	
	public void addFilterAtTypeNotLiteralOr(String tagK,Set<String> tagVs,boolean groupBy) {
		StringBuilder sbFilter = new StringBuilder();
		for(String tagV : tagVs){
			sbFilter.append(tagV).append("|");
		}
//		sbFilter.deleteCharAt(sbFilter.length()-1);
		
		Filter filter = new Filter();
		filter.setFilter(sbFilter.toString());
		filter.setTagk(tagK);
		filter.setGroupBy(groupBy);
		filter.setType("not_literal_or");
		
		addFilter(filter);	
	}
	
	public void addFilterAtTypeWildcard(String tagK,boolean groupBy) {
		Filter filter = new Filter();
		filter.setFilter("*");
		filter.setTagk(tagK);
		filter.setGroupBy(groupBy);
		filter.setType("wildcard");
		
		addFilter(filter);	
	}
	
	public void addFilter(Filter filter){
		if(filters == null){
			filters = new ArrayList<>();
		}
		filters.add(filter);
	}
	
	//|
	public void addTagAtTypeLiteralOr(String tagK,Set<String> tagV){
		addTag(tagK,tagV);
	}
	
	//!|
	public void addTagAtTypeNotLiteralOr(String tagK,Set<String> tagV){
		addTag("!".concat(tagK),tagV);
	}
	
	//*
	public void addTagAtTypeWildcard(String tagK){
		Set<String> hashSet = new HashSet<>();
		hashSet.add("*");
		addTag(tagK,hashSet);
	}
	
	//!*
	public void addTagAtTypeNotAll(String tagK){
		Set<String> hashSet = new HashSet<>();
		tagK = "!".concat(tagK);
		hashSet.add("*");
		addTag(tagK,hashSet);
	}
	
	public void addTag(String tagK, Set<String> tagV) {
		if (tagMap == null) {
			tagMap = new LinkedHashMap<>();
		}
		if(tagV == null){
			tagMap.put(tagK,null);
			return ;
		}
		
		Set<String> tagVs = tagMap.get(tagK);
		if(tagVs == null){
			tagMap.put(tagK, (tagVs = new HashSet<>()));
		}
		tagVs.addAll(tagV);
	}
	
	public List<Filter> getFilters() {
		return filters;
	}
	public LinkedHashMap<String, Set<String>> getTagMap() {
		return tagMap;
	}

}
