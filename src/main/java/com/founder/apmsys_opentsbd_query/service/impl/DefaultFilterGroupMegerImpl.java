package com.founder.apmsys_opentsbd_query.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.founder.apmsys_opentsbd_query.bean.GroupFilter;
import com.founder.apmsys_opentsbd_query.bean.QueryFilter;
import com.founder.apmsys_opentsbd_query.bean.QueryFilterType;
import com.founder.apmsys_opentsbd_query.service.FilterGroupMeger;
import com.founder.apmsys_opentsbd_query.utils.EmptyUtils;


/**
 * 合并filter类
 * @author mr.liang
 */
public class DefaultFilterGroupMegerImpl implements FilterGroupMeger{
	
	private static final Integer TYPE_LITERAL_OR = 1;
	private static final Integer TYPE_NOT_LITERAL_OR = 2;
	private static final Integer TYPE_WILDCARD = 3;
	
	
	private static FilterGroupMeger instance = new DefaultFilterGroupMegerImpl();
	public static final FilterGroupMeger getInstance(){
		return instance;
	}

	/**
	 * 存在3种返回值
	 * 
	 * ==============================================异常=================================
	 * null,表示发生冲突 , 
	 * 
	 * ==============================================正常=================================
	 * [] ,因为groupQueryFilters为空 , 因为没有tag
	 * [xxx],存在Filter
	 */
	@Override
	public List<GroupFilter> meger(List<List<QueryFilter>> groupQueryFilters) {
		List<GroupFilter> groupFilters = new ArrayList<>();
		
		//这种情况 说明完全没有tag或者说tag为*
		if(groupQueryFilters.size() == 0){
			return groupFilters;
		}
		
		for(List<QueryFilter> queryFilters : groupQueryFilters){
			GroupFilter groupFilter = mergeOne(queryFilters);
			if(groupFilter!=null){
				groupFilters.add(groupFilter);
			}
		}
		if(EmptyUtils.isEmpty(groupFilters)){
			return null;
		}
		return groupFilters;
	}
	
	/**
	 * 
	 */
	public GroupFilter mergeOne(List<QueryFilter> queryFilters){
		
		List<QueryFilter> queryFilters0 = new ArrayList<>();
		
		//如果 customTag找不到对应的host，那么不要,忽略它
		for(QueryFilter queryFilter: queryFilters){
			if(queryFilter.isCustomFilter()){
				if(queryFilter.getMatchHosts().size() == 0){
					continue;
				}
			}
			queryFilters0.add(queryFilter);
		}
		//完全没有 那么直接不查询
		if(queryFilters0.size() == 0){
			return null;
		}
		
		//分类
		Map<Integer,List<QueryFilter>> typeAndFiltersMap = new HashMap<>();
		for(QueryFilter queryFilter: queryFilters0){
			//
			QueryFilterType type = queryFilter.getType();
			
			Integer matchType = null;
			if(QueryFilterType.OR.equals(type)){
				matchType = TYPE_LITERAL_OR;
			}
			else if(QueryFilterType.SYS_ALL.equals(type)){
				matchType = TYPE_WILDCARD;
			}
			else if(QueryFilterType.NOT_OR.equals(type) 
					|| QueryFilterType.NOT_ALL.equals(type)){
				matchType = TYPE_NOT_LITERAL_OR;
			}
			
			List<QueryFilter> filterList = typeAndFiltersMap.get(matchType);
			if(filterList == null){
				filterList = new ArrayList<>();
				typeAndFiltersMap.put(matchType,filterList);
			}
			filterList.add(queryFilter);
		}
		
		
		GroupFilter groupQueryFilter = new GroupFilter();
		
		//按照类型 选择不同的合并
		List<QueryFilter> queryTypeliteralOrFilterList = typeAndFiltersMap.get(TYPE_LITERAL_OR);
		if(!parseTypeOr(queryTypeliteralOrFilterList,groupQueryFilter)){
			return null;
		}
		
		List<QueryFilter> queryTypeNotliteralOrFilterList = typeAndFiltersMap.get(TYPE_NOT_LITERAL_OR);
		if(!parseTypeNotOr(queryTypeNotliteralOrFilterList,groupQueryFilter)){
			return null;
		}
		
		List<QueryFilter> queryTypeSysAllFilterList = typeAndFiltersMap.get(TYPE_WILDCARD);
		if(EmptyUtils.isNotEmpty(queryTypeSysAllFilterList)){
			mergeSysAll(queryTypeSysAllFilterList,groupQueryFilter);
		}
		
		
		//按照顺序来添加tag
		for(QueryFilter queryFilter : queryFilters0){
			QueryFilterType type  = queryFilter.getType();
			String 			tagK  = queryFilter.getTagK();
			Set<String> 	tagVs = queryFilter.getTagVs();
			
			if(QueryFilterType.OR.equals(type)){
				groupQueryFilter.addTagAtTypeLiteralOr(tagK, tagVs);
			}
			else if(QueryFilterType.NOT_OR.equals(type)){
				groupQueryFilter.addTagAtTypeNotLiteralOr(tagK, tagVs);
			}
			else if(QueryFilterType.NOT_ALL.equals(type)){
				groupQueryFilter.addTagAtTypeNotAll(tagK);
			}
			else if(QueryFilterType.SYS_ALL.equals(type)){
				groupQueryFilter.addTagAtTypeWildcard(tagK);
			}
		}
		
		return groupQueryFilter;
	}
	
	/**
	 * 过滤与合并 求交集
	 * tag{					=> 	tag{
	 * 		host:101|102		    host:101
	 * 		host:101			}
	 * }
	 * 
	 * tag{					=>	tag{} //抛弃该tag
	 * 		host:101|102
	 * 		host:103
	 * }
	 * 
	 * @return
	 * 返回值为false 表示无法得到返回值
	 * 	例如
	 * 		tag{
	 * 			host: 101|102
	 * 			host: 103
	 * 		}
	 * host交集明显为空 这种不查opentsdb
	 */
	boolean parseTypeOr(List<QueryFilter> orQueryFilterList,GroupFilter groupQueryFilter){
		// 如果是空,那么直接通过
		if(EmptyUtils.isEmpty(orQueryFilterList)){
			return true;
		}
		// 拿到转换成sys的tagKV  Map<TagK,List<TagVs>
		Map<String, List<Set<String>>> sysTagKVsMap = parseFilterCreateSysTagKVsMap(orQueryFilterList);
		
		// 过滤,求交集
		Map<String, Set<String>> tagKAndTagVsMap = new HashMap<>();
		for (Entry<String, List<Set<String>>> entry : sysTagKVsMap.entrySet()) {
			String 			  tagK 		= entry.getKey();
			List<Set<String>> tagVsList = entry.getValue();

			Set<String> valuesSet = new HashSet<>();
			for (int i = 0; i < tagVsList.size(); i++) {
				Set<String> tagVs = tagVsList.get(i);
				if (i == 0) {
					valuesSet.addAll(tagVs);
				} else {
					valuesSet.retainAll(tagVs);
					if (valuesSet.size() == 0) {// 不断的求交集,如果交集为空，那么不要这个filter
						return false;// 这里属于交集为空，被干掉的情况
					}
				}
			}
			
			tagKAndTagVsMap.put(tagK, valuesSet);
		}
		
		// 如果为null或者空，那么说明出现冲突，可以不要了
		if(EmptyUtils.isEmpty(tagKAndTagVsMap)){
			return false;
		}
		//检查是否应该分组
		boolean groupBy = checkGroupBy(orQueryFilterList);
		//添加filter
		for(Entry<String, Set<String>> entry : tagKAndTagVsMap.entrySet()){
			groupQueryFilter.addFilterAtTypeLiteralOr(
					entry.getKey(), entry.getValue(), groupBy);
		}
		return true;
	}
	
	/**
	 * 过滤与合并  求并集
	 * tag{					=> 	tag{
	 * 		host:101|102		    host:101|102
	 * 		host:101			}
	 * }
	 * 
	 * tag{					=>	tag{
	 * 		host:101|102			host:101|102|103
	 * 		host:103			}
	 * }
	 * 
	 * @return 这里返回一定是true
	 */
	boolean parseTypeNotOr(List<QueryFilter> notOrQueryFilterList,GroupFilter groupQueryFilter){
		// 如果是空,那么直接通过
		if(EmptyUtils.isEmpty(notOrQueryFilterList)){
			return true;
		}
		
		// 合并
		// 拿到转换成sys的tagKV  Map<TagK,List<TagVs>
		Map<String, List<Set<String>>> sysTagKVsMap = parseFilterCreateSysTagKVsMap(notOrQueryFilterList);
		
		// 过滤
		Map<String, Set<String>> tagKAndTagVsMap = new HashMap<>();
		for (Entry<String, List<Set<String>>> entry : sysTagKVsMap.entrySet()) {
			String 			  tagK 		= entry.getKey();
			List<Set<String>> tagVsList = entry.getValue();

			Set<String> valuesSet = new HashSet<>();
			for (int i = 0; i < tagVsList.size(); i++) {
				Set<String> tagVs = tagVsList.get(i);
				valuesSet.addAll(tagVs);//求并集，这个就很爽了
			}
			tagKAndTagVsMap.put(tagK, valuesSet);
		}
				
		//对于!tag这种情况,这里必须是false,不存在true的情况
		boolean groupBy = false;
		
		//添加filter
		for(Entry<String, Set<String>> entry : tagKAndTagVsMap.entrySet()){
			groupQueryFilter.addFilterAtTypeNotLiteralOr(
					entry.getKey(), entry.getValue(), groupBy);
		}
		return true;
	}
	
	/**
	 * host:*
	 * table:* 这种
	 */
	boolean mergeSysAll(List<QueryFilter> queryFilterList,GroupFilter groupQueryFilter){
		boolean groupBy = checkGroupBy(queryFilterList);//理论上 这种filter一定为true,因为只可能在单个by的时候出现
		for (QueryFilter queryFilter : queryFilterList) {
			groupQueryFilter.addFilterAtTypeWildcard(queryFilter.getTagK(), groupBy);
		}
		return true;
	}
	
	
	/**
	 * 解析QueryFiltrs 
	 * 1.排除自定义tag,转换成对应的sysTag
	 * 
	 * 例如
	 * 		  address:wuahn		level:1
	 * host:[ [102,103], 		[103,104] ]
	 * 
	 * @param queryFilterList
	 */
	Map<String, List<Set<String>>> parseFilterCreateSysTagKVsMap(List<QueryFilter> queryFilterList){
		Map<String, List<Set<String>>> sysTagKVsMap = new HashMap<>();
		
		for (QueryFilter queryFilter : queryFilterList) {
			String 		tempTagK;
			Set<String> tempTagVs;

			if (queryFilter.isCustomFilter()) {
				tempTagK = "host";
				tempTagVs = queryFilter.getMatchHosts();
			} else {
				tempTagK = queryFilter.getTagK();
				tempTagVs = queryFilter.getTagVs();
			}

			List<Set<String>> tagVsList = sysTagKVsMap.get(tempTagK);
			if (tagVsList == null) {
				tagVsList = new ArrayList<>();
				sysTagKVsMap.put(tempTagK, tagVsList);
			}
			tagVsList.add(tempTagVs);
		}
		
		return sysTagKVsMap;
	}
	
	/**
	 * 有一个queryFilter的 groupBy为true，那么其他的都为true
	 * 
	 * 具体的判断 是否为 true，应该在 {@link DefaultBatchQueryServiceImpl}实现
	 */
	boolean checkGroupBy(List<QueryFilter> queryFilterList){
		for(QueryFilter queryFilter:queryFilterList){
			if(queryFilter.isGroupBy()){
				return true;
			}
		}
		return false;
	}

}
