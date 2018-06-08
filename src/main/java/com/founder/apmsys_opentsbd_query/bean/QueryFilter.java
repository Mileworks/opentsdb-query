package com.founder.apmsys_opentsbd_query.bean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 
 * @author mr.liang
 */
public class QueryFilter {

	private QueryFilterType  type ;
	private String 		     tagK ;
	private Set<String>      tagVs;
	private Set<String>      matchHosts;
	private boolean			 groupBy;//自定义Tag永远false ，系统tag可能会true
	
	/**
	 * 判断是否为自定义tagK ， 
	 * @see {@link #addMatchHosts} 
	 * @see	{@link #setMatchHosts}
	 * @see	{@link #isCustomFilter}
	 */
	private boolean  isCustomFilter;
	
	public QueryFilter(QueryFilterType type) {
		this.type = type;
	}
	
	//这里host同custom
	public boolean isCustomFilter(){
		return isCustomFilter;
	}

	public void addTagV(String tagV){
		if(tagVs == null){
			tagVs = new LinkedHashSet<>();
		}
		tagVs.add(tagV);
	}
	
	public void addMatchHosts(Set<String> hosts){
		isCustomFilter = true ;
		if(this.matchHosts == null){
			this.matchHosts = new LinkedHashSet<>();
		}
		this.matchHosts.addAll(hosts);
	}
	public void setMatchHosts(Set<String> hosts) {
		isCustomFilter = true ;
		this.matchHosts = hosts;
	}
	
	public QueryFilterType getType() {
		return type;
	}
	public void setType(QueryFilterType type) {
		this.type = type;
	}
	public String getTagK() {
		return tagK;
	}
	public void setTagK(String tagK) {
		this.tagK = tagK;
	}
	public Set<String> getTagVs() {
		return tagVs;
	}
	public void setTagVs(Set<String> tagVs) {
		this.tagVs = tagVs;
	}
	public Set<String> getMatchHosts() {
		return matchHosts;
	}
	
	public boolean isGroupBy() {
		return groupBy;
	}
	public void setGroupBy(boolean groupBy) {
		this.groupBy = groupBy;
	}
}
