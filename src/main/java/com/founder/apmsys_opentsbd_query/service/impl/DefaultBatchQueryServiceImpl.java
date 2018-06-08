package com.founder.apmsys_opentsbd_query.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.founder.apmsys_opentsbd_query.bean.GroupFilter;
import com.founder.apmsys_opentsbd_query.bean.QueryFilter;
import com.founder.apmsys_opentsbd_query.mapping.TagAndHostMapping;
import com.founder.apmsys_opentsbd_query.mapping.TagAndHostMappingFactory;
import com.founder.apmsys_opentsbd_query.param.QueryBatchParam;
import com.founder.apmsys_opentsbd_query.param.QueryBatchParams;
import com.founder.apmsys_opentsbd_query.parse.QuerySqlPaser;
import com.founder.apmsys_opentsbd_query.parse.QuerySqlPaser.QuerySqlPaserResult;
import com.founder.apmsys_opentsbd_query.result.QueryResult;
import com.founder.apmsys_opentsbd_query.service.AccessOpentsdbService;
import com.founder.apmsys_opentsbd_query.service.BatchQueryService;
import com.founder.apmsys_opentsbd_query.service.FilterGroupMeger;
import com.founder.apmsys_opentsbd_query.service.FilterParse;
import com.founder.apmsys_opentsbd_query.test.Debug;


/**
 * 默认的实现
 * 
 * 这种实现 
 * <ul>
 * 	<li>
 * 		1.
 * 			一个metric 就访问一次opentsdb!
 * 			即 : 一个 	{@link QueryBatchParam} 访问一次opentsdb!
 * 	</li>
 * 	<li>
 * 		2.
 * 			每次方法调用 都会访问有且仅有一次 redis!
 * 	</li>
 * </ul>
 * 访问数据库次数 = {@code QueryBatchParams.getQueries().size() + 1}
 * 
 * @author mr.liang
 */
public class DefaultBatchQueryServiceImpl implements BatchQueryService{

	private TagAndHostMappingFactory tagAndHostMappingFactory;
	
	public DefaultBatchQueryServiceImpl(TagAndHostMappingFactory tagAndHostMappingFactory){
		this.tagAndHostMappingFactory = tagAndHostMappingFactory;
	}
	
	@Override
	public List<QueryResult> batchQuery(QueryBatchParams queryBatchParams) {
		String userId = queryBatchParams.getUserId();
		
		//tag和Host的映射类，目的在于减少请求次数
		TagAndHostMapping tagAndHostMapping = null;
		
		List<QueryResult> queryResultList = new ArrayList<>();
		
		for (QueryBatchParam query : queryBatchParams.getQueries()) {
			// 解析表达式类
			QuerySqlPaserResult querySql = QuerySqlPaser.parse(query.getQ());
			
			//tag和Host的映射类，目的在于减少请求次数
			if(tagAndHostMapping == null && querySql.isContainCustomTag()){//如果存在customTag
				tagAndHostMapping = tagAndHostMappingFactory.getTagAndHostMapping(userId);
			}
			
			// 解析 
			FilterParse filterparse = new DefaultFilterParseImpl(querySql,tagAndHostMapping);
			List<List<QueryFilter>> groupFilter = filterparse.parse();
			Debug.setFilterBefore(groupFilter);
			
			// 合并
			FilterGroupMeger fgm = DefaultFilterGroupMegerImpl.getInstance();
			List<GroupFilter> megerFilters = fgm.meger(groupFilter);
			
			//添加权限
			if(megerFilters != null) {
				if(megerFilters.size() == 0){
					GroupFilter groupFilte = new GroupFilter();
					groupFilte.addFilterAtTypeLiteralOr("uid", userId);
					megerFilters.add(groupFilte);
				} else {
					for(GroupFilter groupFilters : megerFilters){
						groupFilters.addFilterAtTypeLiteralOr("uid", userId);
					}
				}
			}
			
			AccessOpentsdbService accessOpentsdbService = 
					new DefaultAccessOpentsdbServiceImpl(query, querySql, megerFilters);
			QueryResult queryResult = accessOpentsdbService.access();
			
			queryResultList.add(queryResult);
		}
		
		return queryResultList;
	}
	
	
	@Override
	public QueryResult singleQuery(QueryBatchParam queryParam, String userId) {
		Debug.clear();
		
		// 解析表达式类
		QuerySqlPaserResult querySql = QuerySqlPaser.parse(queryParam.getQ());
		Debug.setQuerySql(querySql.getQuerySql());
		
		//tag和Host的映射类，目的在于减少请求次数
		TagAndHostMapping tagAndHostMapping = null;
		if(querySql.isContainCustomTag()){//如果存在customTag
			Debug.setQueryRedis();
			tagAndHostMapping = tagAndHostMappingFactory.getTagAndHostMapping(userId);
		}
		
		// 解析 
		FilterParse filterparse = new DefaultFilterParseImpl(querySql,tagAndHostMapping);
		List<List<QueryFilter>> groupFilter = filterparse.parse();
		Debug.setFilterBefore(groupFilter);
		
		// 合并
		FilterGroupMeger fgm = DefaultFilterGroupMegerImpl.getInstance();
		List<GroupFilter> megerFilters = fgm.meger(groupFilter);
		
		//添加权限
		if(megerFilters != null){
			if(megerFilters.size() == 0){
				GroupFilter groupFilte = new GroupFilter();
				groupFilte.addFilterAtTypeLiteralOr("uid", userId);
				megerFilters.add(groupFilte);
			} else {
				for(GroupFilter groupFilters : megerFilters){
					groupFilters.addFilterAtTypeLiteralOr("uid", userId);
				}
			}
		}
		
		Debug.setFilterAlfer(megerFilters);
		
		AccessOpentsdbService accessOpentsdbService = 
				new DefaultAccessOpentsdbServiceImpl(queryParam, querySql, megerFilters);
		QueryResult queryResult = accessOpentsdbService.access();
		
		Debug.setResult(queryResult);
		return queryResult;
	}
}