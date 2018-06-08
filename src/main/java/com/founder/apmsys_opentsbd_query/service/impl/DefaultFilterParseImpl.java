package com.founder.apmsys_opentsbd_query.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.founder.apmsys_opentsbd_query.bean.QueryFilter;
import com.founder.apmsys_opentsbd_query.bean.QueryFilterType;
import com.founder.apmsys_opentsbd_query.mapping.TagAndHostMapping;
import com.founder.apmsys_opentsbd_query.parse.QuerySqlPaser.QuerySqlPaserResult;
import com.founder.apmsys_opentsbd_query.service.FilterParse;
import com.founder.apmsys_opentsbd_query.utils.EmptyUtils;



/**
 * 默认的实现
 * 
 * 整理参数
 * 
 * Unique ==>表示 from和by 仅出现一次
 * Repeat ==>表示 from和by 出现多次
 * 
 * @author mr.liang
 */
public class DefaultFilterParseImpl implements FilterParse{

	//解析单条参数
	private QuerySqlPaserResult querySqlPaserResult;
	//tag与host映射
	private TagAndHostMapping   tagAndHostMapping;
	
	public DefaultFilterParseImpl(QuerySqlPaserResult querySqlPaserResult,
			TagAndHostMapping tagAndHostMapping) {
		this.querySqlPaserResult = querySqlPaserResult;
		this.tagAndHostMapping = tagAndHostMapping;
	}
	
	@Override
	public List<List<QueryFilter>> parse(){
		//解析多种情况 需要自由组合tag
		List<List<QueryFilter>> filterGroup = new ArrayList<>();
		filterGroup.addAll(parseUniqueCusBy());
		filterGroup.addAll(parseRepeatCusFromTag());
		//自由组合
		List<List<QueryFilter>> combinationQueryFilterGroup =  combination(filterGroup);
		
		List<QueryFilter> constantGroupList = new ArrayList<>();
		constantGroupList.addAll(parseUniqueSysBySet());
		
		constantGroupList.addAll(parseUniqueCusFromTag());
		constantGroupList.addAll(parseUniqueSysFromTag());
		constantGroupList.addAll(parseRepeatSysFromTag());
		constantGroupList.addAll(parseCusAgentTags());
		
		constantGroupList.addAll(parseCusNoTagMap());
		constantGroupList.addAll(parseSysNoTagMap());
		constantGroupList.addAll(parseCusNoAgentTags());
		
		//合并
		if(EmptyUtils.isEmpty(combinationQueryFilterGroup)
				&& EmptyUtils.isNotEmpty(constantGroupList)){
			combinationQueryFilterGroup.add(constantGroupList);
		}
		else{
			for(List<QueryFilter> queryFilterGroup :combinationQueryFilterGroup){
				queryFilterGroup.addAll(constantGroupList);
			}
		}
		
		return combinationQueryFilterGroup;
	}

	/**
	 * 仅仅出现在 from里的tag, 这种tag 充当"过滤"和"选择"的重要
	 * 
	 * 例如
	 * 过滤1. : from {address:wuhan,level:1}by{address}			
	 * 			==> displayName : address:wuhan,level:1   ,address:bj,level:1
	 * 过滤2. : from {address:wuhan,level:1,level:2}by{address}  
	 * 			==> displayName : address:wuhan,level:1|2 ,address:bj,level:1|2
	 */
	public List<QueryFilter> parseUniqueCusFromTag() {
		List<QueryFilter> groupQueryFilter = new ArrayList<>();
		
		Map<String, Set<String>> uniqueCusFromTagMap = querySqlPaserResult.getUniqueCusFromTagMap();
		if (EmptyUtils.isNotEmpty(uniqueCusFromTagMap)) {
			for (Entry<String, Set<String>> entry : uniqueCusFromTagMap.entrySet()) {
				String 		tagK    = entry.getKey();
				Set<String> tagVSet = entry.getValue();
				
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.OR);
				queryFilter.setTagK(tagK);
				for (String tagV : tagVSet) {
					Set<String> hosts = tagAndHostMapping.queryCustomByCustomTagKV(tagK, tagV);
					queryFilter.addTagV(tagV);
					queryFilter.addMatchHosts(hosts);
				}
				
				groupQueryFilter.add(queryFilter);
			}
		}
		return groupQueryFilter;
	}
	
	/**
	 * 在from和by里面都出现了，这表示我们要对他分组!
	 * 
	 * 具体分组:
	 * 	address 包含 wuhan, beijing, tianjing
	 *  	
	 *  from {address:wuhan,level:1}by{address}
	 *  	==>address=wuhan ,level:1
	 *  	==>address!=wuhan,level:1
	 *  
	 *  from {address:wuhan,address:beijing,level:1}by{address}
	 *  	==>address=wuhan,level:1
	 *  	==>address=beijing,level:1
	 *  	==>address!=wuhan|beijing,level:1
	 *  
	 *  from {address:wuhan,address:beijing,address:tianjing,level:1}by{address}
	 *  	==>address=wuhan,level:1
	 *  	==>address=beijing,level:1
	 *  	==>address=tianjing,level:1
	 *  	==>address!=wuhan|beijing|tianjing,level:1
	 *  
	 *  from {level:1}by{address}   这种情况在本方法不会初选 ，在 unique_cus_by的情况的会出现，{@link #parseUniqueCusBy}
	 *  	==>address=wuhan,level:1
	 *  	==>address=beijing,level:1
	 *  	==>address=tianjing,level:1
	 *  	==>address!=*,level:1
	 */
	private List<List<QueryFilter>> parseRepeatCusFromTag (){
		//List<QueryFilter>     ==>          tagK相同的  QueryFilter分组
		//queryFilterGroupGroup ==> 不同tagK中  tagK相同的  QueryFilter分组
		List<List<QueryFilter>> queryFilterGroupGroup = new ArrayList<>();
		
		Map<String, Set<String>> repeatCusFromTagMap = querySqlPaserResult.getRepeatCusFromTagMap();
		
		if (EmptyUtils.isNotEmpty(repeatCusFromTagMap)) {
			for (Entry<String, Set<String>> entry : repeatCusFromTagMap.entrySet()) {
				
				//tagK相同的的QueryFilter
				List<QueryFilter> sameTagkQueryFilterGroup = new ArrayList<>();
				
				Set<String> allHosts = new HashSet<>();
				
				String      tagK   = entry.getKey();
				Set<String> tagVs  = entry.getValue();
				
				for (String tagV : tagVs) {
					Set<String> hosts = tagAndHostMapping.queryCustomByCustomTagKV(tagK, tagV);
					
					QueryFilter queryFilter = new QueryFilter(QueryFilterType.OR); 
					queryFilter.setTagK(tagK);
					queryFilter.addTagV(tagV);
					queryFilter.addMatchHosts(hosts);
					sameTagkQueryFilterGroup.add(queryFilter);
					
					allHosts.addAll(hosts);
				}
				
				//取!
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.NOT_OR); 
				queryFilter.setTagK(tagK);
				queryFilter.setTagVs(tagVs);
				queryFilter.setMatchHosts(allHosts);
				sameTagkQueryFilterGroup.add(queryFilter);
				
				queryFilterGroupGroup.add(sameTagkQueryFilterGroup);
			}
		}
		
		return queryFilterGroupGroup;
	}
	
	
	
	/**
	 * cus在by处唯一 ，那么表示用户想对 by完全分组
	 * 	具体分组
	 * 	address 包含 wuhan, beijing, tianjing
	 * 
	 *  from {level:1}by{address}
	 *  	==>address=wuhan,level:1
	 *  	==>address=beijing,level:1
	 *  	==>address=tianjing,level:1
	 *  	==>address!=*,level:1
	 */
	public List<List<QueryFilter>> parseUniqueCusBy(){
		//List<QueryFilter>     ==>          tagK相同的  QueryFilter分组
		//queryFilterGroupGroup ==> 不同tagK中  tagK相同的  QueryFilter分组
		List<List<QueryFilter>> queryFilterGroupGroup = new ArrayList<>();
		
		Set<String> uniqueCus = querySqlPaserResult.getUniqueCusBySet();
		
		if (EmptyUtils.isNotEmpty(uniqueCus)) {
			for (String uniqueCusTagK : uniqueCus) {
				//tagK相同的的QueryFilter
				List<QueryFilter> sameTagkQueryFilterGroup = new ArrayList<>();
				
				//这里不需要顺序
				Set<String> allHost = new HashSet<>();
				Map<String, Set<String>> tagVHosts = tagAndHostMapping.queryCustomByCustomTagK(uniqueCusTagK);

				for (Entry<String, Set<String>> entry : tagVHosts.entrySet()) {
					String      tagV  = entry.getKey();
					Set<String> hosts = entry.getValue();
					
					QueryFilter queryFilter = new QueryFilter(QueryFilterType.OR);
					queryFilter.setTagK(uniqueCusTagK);
					queryFilter.addTagV(tagV);
					queryFilter.addMatchHosts(hosts);
					
					sameTagkQueryFilterGroup.add(queryFilter);
					allHost.addAll(hosts);
				}
				
				// !N
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.NOT_ALL);
				queryFilter.setTagK(uniqueCusTagK);
				queryFilter.addMatchHosts(allHost);
				sameTagkQueryFilterGroup.add(queryFilter);
				
				queryFilterGroupGroup.add(sameTagkQueryFilterGroup);
			}
		}
		return queryFilterGroupGroup;
	}
	
	/**
	 * 这个组合 属于 不同的tagK间的自由组合
	 */
	public List<List<QueryFilter>> combination(List<List<QueryFilter>> filterGroup){
		
		List<List<QueryFilter>> queryFilterGroup = new ArrayList<>();
		
		for (int i = 0; i < filterGroup.size(); i++) {
			List<QueryFilter> sameTagkQueryFilterGroup = filterGroup.get(i);
			
			
			List<List<QueryFilter>> tempFilterGroup = new ArrayList<>();
			
			for(QueryFilter queryFilter : sameTagkQueryFilterGroup){
				if (i == 0) {// 形成一个个新的组
					List<QueryFilter> groupFilter = new ArrayList<>();
					groupFilter.add(queryFilter);
					tempFilterGroup.add(groupFilter);
				}
				else {
					//分组添加
					for(List<QueryFilter> queryFilters  : queryFilterGroup){
						List<QueryFilter>groupFilter = new ArrayList<>();
						groupFilter.addAll(queryFilters);
						groupFilter.add(queryFilter);
						tempFilterGroup.add(groupFilter);
					}
				}
			}
			
			queryFilterGroup = tempFilterGroup;
		}
		
		return queryFilterGroup;
	}
	
	
	/**
	 * 每个groupFilter叠加
	 * by =>false
	 */
	public List<QueryFilter> parseUniqueSysFromTag(){
		Map<String, Set<String>> uniqueSysFromTagMap = querySqlPaserResult.getUniqueSysFromTagMap();
		
		List<QueryFilter> uniqueSysFilter = new ArrayList<>();
		
		if (EmptyUtils.isNotEmpty(uniqueSysFromTagMap)) {
			for (Entry<String, Set<String>> entry : uniqueSysFromTagMap.entrySet()) {
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.OR);
				queryFilter.setTagK(entry.getKey());
				queryFilter.setTagVs(entry.getValue());
				uniqueSysFilter.add(queryFilter);
			}
		}
		return uniqueSysFilter;
	}
	
	
	/**
	 * 每个groupFilter叠加
	 * by true
	 * @return 
	 */
	public List<QueryFilter> parseRepeatSysFromTag() {
		Map<String, Set<String>> repeatSysFromTagMap = querySqlPaserResult.getRepeatSysFromTagMap();
		
		List<QueryFilter> repeatSysFilter = new ArrayList<>();
		
		if (EmptyUtils.isNotEmpty(repeatSysFromTagMap)) {
			for (Entry<String, Set<String>> entry : repeatSysFromTagMap.entrySet()) {
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.OR);
				queryFilter.setTagK(entry.getKey());
				queryFilter.setTagVs(entry.getValue());
				queryFilter.setGroupBy(true);
				repeatSysFilter.add(queryFilter);
			}
		}
		return repeatSysFilter;
	}
	
	/**
	 * 解析标准的agentTag
	 */
	private List<QueryFilter> parseCusAgentTags(){
		Set<String> agentTags = querySqlPaserResult.getCusAgentTags();
		
		List<QueryFilter> repeatSysFilter = new ArrayList<>();
		
		if (EmptyUtils.isNotEmpty(agentTags)) {
			for(String agentTag : agentTags){
				Set<String> hosts = tagAndHostMapping.queryHostsByAgentTagK(agentTag);
				
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.OR);
				queryFilter.setTagK(agentTag);
				queryFilter.setMatchHosts(hosts);
				repeatSysFilter.add(queryFilter);
			}
		}
		return repeatSysFilter;
	}
	
	
	/**
	 * 每个groupFilter叠加
	 *     tagK : *
	 */
	public List<QueryFilter> parseUniqueSysBySet() {
		Set<String> uniqueSysBySet = querySqlPaserResult.getUniqueSysBySet();
		
		List<QueryFilter> uniqueSysByFilter = new ArrayList<>();
		if (EmptyUtils.isNotEmpty(uniqueSysBySet)) {
			for (String tagK : uniqueSysBySet) {
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.SYS_ALL);
				queryFilter.setTagK(tagK);
				queryFilter.setGroupBy(true);
				uniqueSysByFilter.add(queryFilter);
			}
		}
		return uniqueSysByFilter;
	}
	
	/**
	 * 自定义的 !tag
	 */
	public List<QueryFilter> parseCusNoTagMap(){
		Map<String, Set<String>> cusNoTagMap = querySqlPaserResult.getCusNoTagMap();
		
		List<QueryFilter> cusNoTagMapFilter = new ArrayList<>();
		
		if(EmptyUtils.isNotEmpty(cusNoTagMap)){
			for(Entry<String, Set<String>> entry:cusNoTagMap.entrySet()){
				
				String      tagK   = entry.getKey();
				Set<String> tagVs  = entry.getValue();
				
				Set<String> allHosts = new HashSet<>();
				for(String tagV : tagVs){
					Set<String> hosts = tagAndHostMapping.queryCustomByCustomTagKV(tagK, tagV);
					allHosts.addAll(hosts);
				}
				
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.NOT_OR);
				queryFilter.setTagK(tagK);
				queryFilter.setTagVs(tagVs);
				queryFilter.setMatchHosts(allHosts);
				cusNoTagMapFilter.add(queryFilter);
			}
		}
		return cusNoTagMapFilter;
	}
	
	/**
	 * sys  !tag
	 */
	public List<QueryFilter> parseSysNoTagMap(){
		Map<String, Set<String>> sysNoTagMap = querySqlPaserResult.getSysNoTagMap();
		
		List<QueryFilter> sysNoTagMapFilter = new ArrayList<>();
		
		if(EmptyUtils.isNotEmpty(sysNoTagMap)){
			for(Entry<String, Set<String>> entry:sysNoTagMap.entrySet()){
				QueryFilter queryFilter = new QueryFilter(QueryFilterType.NOT_OR);
				queryFilter.setTagK(entry.getKey());
				queryFilter.setTagVs(entry.getValue());
				sysNoTagMapFilter.add(queryFilter);
			}
		}
		return sysNoTagMapFilter;
	}

	/**
	 * 解析标准的agentTag
	 */
	private List<QueryFilter> parseCusNoAgentTags(){
		Set<String> noAgentTags = querySqlPaserResult.getCusNoAgentTags();
		
		List<QueryFilter> repeatSysFilter = new ArrayList<>();
		
		if (EmptyUtils.isNotEmpty(noAgentTags)) {
			for (String agentTag : noAgentTags) {
				Set<String> hosts = tagAndHostMapping.queryHostsByAgentTagK(agentTag);

				QueryFilter queryFilter = new QueryFilter(QueryFilterType.NOT_OR);
				queryFilter.setTagK(agentTag);
				queryFilter.setMatchHosts(hosts);
				repeatSysFilter.add(queryFilter);
			}
		}
		return repeatSysFilter;
	}
	
}
