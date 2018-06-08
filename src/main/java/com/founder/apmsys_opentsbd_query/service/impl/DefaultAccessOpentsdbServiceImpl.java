package com.founder.apmsys_opentsbd_query.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.founder.apmsys_opentsbd_query.bean.GroupFilter;
import com.founder.apmsys_opentsbd_query.param.QueryBatchParam;
import com.founder.apmsys_opentsbd_query.parse.QuerySqlPaser.QuerySqlPaserResult;
import com.founder.apmsys_opentsbd_query.result.QueryResult;
import com.founder.apmsys_opentsbd_query.result.QueryResultSeries;
import com.founder.apmsys_opentsbd_query.service.AccessOpentsdbService;
import com.founder.apmsys_opentsbd_query.test.Debug;
import com.founder.apmsys_opentsbd_query.utils.EmptyUtils;
import com.founder.apmsys_opentsdb.api.QueryExpRequest;
import com.founder.apmsys_opentsdb.param.Downsampler;
import com.founder.apmsys_opentsdb.param.FillPolicy;
import com.founder.apmsys_opentsdb.param.FillPolicyEnum;
import com.founder.apmsys_opentsdb.param.Filters;
import com.founder.apmsys_opentsdb.param.Metric;
import com.founder.apmsys_opentsdb.param.Outputs;
import com.founder.apmsys_opentsdb.param.QueryExpReqDto;
import com.founder.apmsys_opentsdb.param.Time;
import com.founder.apmsys_opentsdb.reponse.Meta;
import com.founder.apmsys_opentsdb.reponse.QueryExpResDto;

public class DefaultAccessOpentsdbServiceImpl implements AccessOpentsdbService{
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultAccessOpentsdbServiceImpl.class);
	
	private QuerySqlPaserResult querySql ;
	private List<GroupFilter>   groupFilters;
	private QueryBatchParam     queryParam;
	
	private List<Filters> filtersList = new ArrayList<>();
	private List<Metric>  metrics = new ArrayList<>();
	private List<Outputs> outputs = new ArrayList<>();
	
	private Map<String,Map<String, Set<String>>> idAndTagMapMapping = new HashMap<>();
	
	public DefaultAccessOpentsdbServiceImpl(QueryBatchParam queryParam, QuerySqlPaserResult querySql,
			List<GroupFilter> groupFilters) {
		this.queryParam = queryParam;
		this.querySql = querySql;
		this.groupFilters = groupFilters;

		//如果为null ，那么直接返回结果
		if(groupFilters!=null){
			this.init();
		}
	}
	
	@Override
	public QueryResult access(){
		
		try {
			QueryExpReqDto httpParam = createHttpParam();
			Debug.setHttpParam(httpParam);
			if(httpParam == null){
				return createQueryResult("ok",new ArrayList<>()); 
			}
			
			QueryExpResDto queryExpResDto = new QueryExpRequest().setHttpParam(httpParam).sendHttpRequest();
			Debug.setOpentsdbResult(queryExpResDto);
			
			List<QueryResultSeries> queryResultSeries = converResult(queryExpResDto);
			
			return createQueryResult("ok",queryResultSeries);
		} catch (Exception e) {
			Debug.setOpentsdbResult("javacode:"+e.toString());
			logger.error(JSON.toJSONString(queryParam),e);
			
			return createQueryResult("error",new ArrayList<>());
		}
	}
	
	private QueryResult createQueryResult(String status ,List<QueryResultSeries> queryResultSeries){
		QueryResult queryResult = new QueryResult();
		queryResult.setStatus(status);
		queryResult.setQuery(querySql.getQuerySql());
		queryResult.setFrom(queryParam.getBegin());
		queryResult.setTo(queryParam.getEnd());
		queryResult.setAttributes(queryParam.getAttributes());
		queryResult.setSeries(queryResultSeries);
		return queryResult;
	}
	
	
	/**
	 * 转换返回值成对应的series
	 */
	private List<QueryResultSeries> converResult(QueryExpResDto queryExpResDto){
		List<QueryResultSeries> queryResultSeries = new ArrayList<>();
		for(com.founder.apmsys_opentsdb.reponse.Outputs outputs : queryExpResDto.getOutputs()){
			//总的series的数量
			Integer 		   		seriesSize   = outputs.getDpsMeta().getSeries();
			
			//displayNames
			String  		         id 		    = outputs.getId();
			List<Meta>		   		 metas		    = outputs.getMeta();
			Map<String, Set<String>> tagMaps   	    = idAndTagMapMapping.get(id);
			List<Map<String,String>> displayTagMaps = createDisplayTagMaps(tagMaps,metas);
			
			//pointMaps
			List<List<Double>> 		dps 	     = outputs.getDps();
			List<Map<Long,Double>>  pointMaps 	 = converPointMaps(dps,seriesSize);
			
			for(int i=0;i<seriesSize;i++){
				Map<String,String> displayTagMap = displayTagMaps.get(i);
				Map<Long,Double>   pointMap 	 = pointMaps.get(i);
				
				if(EmptyUtils.isEmpty(pointMap)){//如果点为空，那么也不需要返回
					break;
				}
				
				QueryResultSeries series = new QueryResultSeries();
				series.setTags(displayTagMap);
				series.setDisplayName(createDisplayName(displayTagMap));
				series.setPointlist(pointMap);
				series.setStart(queryParam.getBegin());
				series.setEnd(queryParam.getEnd());
				series.setInterval(queryParam.getInterval());
				
				queryResultSeries.add(series);
			}
		}
		
		return queryResultSeries;
	}
	
	/**
	 * 创建多个displayTagMap
	 * @param tagMaps
	 * @param metas
	 * @return
	 */
	private List<Map<String,String>> createDisplayTagMaps(Map<String, Set<String>> tagMaps,List<Meta> metas){
		if(EmptyUtils.isEmpty(metas)){
			return null;
		}
		List<Map<String,String>> tagMapList = new ArrayList<>();
		//0是timestamp
		for(int i=1;i<metas.size();i++){
			Meta meta = metas.get(i);
			Map<String, String> commonTags = meta.getCommonTags();
			Map<String,String> tagMap = createDisplayTagMap(tagMaps,commonTags);
			tagMapList.add(tagMap);
		}
		return tagMapList;
	}
	
	/**
	 * 创建单个displayTagMap
	 */
	private Map<String,String> createDisplayTagMap(Map<String, Set<String>> tagMaps,Map<String, String> commonTags){
		Map<String,String> tagMap = new LinkedHashMap<>();
		if(tagMaps == null){//这种对应 {*}
			return tagMap;
		}
		
		a:for(Entry<String, Set<String>> tagMapEntry:tagMaps.entrySet()){
			String tagK = tagMapEntry.getKey();
			
			for(Entry<String, String> commonTagsEntry: commonTags.entrySet()){
				String commonTagsK = commonTagsEntry.getKey();
				if(tagK.equals(commonTagsK)){
					tagMap.put(tagK, commonTagsEntry.getValue());
					continue a;
				}
			}
			
			if(tagMapEntry.getValue() != null){
				StringBuffer tagVBuf = new StringBuffer();
				for(String tagV : tagMapEntry.getValue()){
					tagVBuf.append("|").append(tagV);
				}
				tagVBuf.deleteCharAt(0);
				tagMap.put(tagK, tagVBuf.toString());
			}else{
				tagMap.put(tagK, null);
			}
			
		}
		return tagMap;
	}
	
	/**
	 * 如果tagMaps含有，那么覆盖!
	 * 如果没有，那么就无视
	 */
	private String createDisplayName(Map<String,String> displayTagMap){
		String tags = null;
		
		if(EmptyUtils.isEmpty(displayTagMap)){
			tags = "*";
		}else{
			StringBuffer tagBuf = new StringBuffer();
			
			for(Entry<String, String> entry : displayTagMap.entrySet()){
				String displayKey = entry.getKey();
				String displayVal = entry.getValue();
				
				if(displayVal == null){
					tagBuf.append(",").append(displayKey);
				}else{
					tagBuf.append(",").append(displayKey).append(":").append(displayVal);
				}
			}
			tagBuf.deleteCharAt(0);
			tags = tagBuf.toString();
		}
		
		return new StringBuffer()
			.append(querySql.getAggregator())
			.append(querySql.getRate()?":rate":"")
			.append(":").append(querySql.getMetric())
			.append("{").append(tags).append("}")
			.toString();
	}
	
	/**
	 * 初始化加载
	 */
	private void init(){
		for (int i = 0; i < groupFilters.size(); i++) {
			GroupFilter groupFilter = groupFilters.get(i);
			
			String id = "a"+i;
			
			this.idAndTagMapMapping.put(id, groupFilter.getTagMap());
			
			Filters filters = new Filters();
			filters.setId(id);
			filters.setTags(groupFilter.getFilters());
			this.filtersList.add(filters);
			
			Metric metric = new Metric();
			metric.setId(id);
			metric.setMetric(querySql.getMetric());
			metric.setFilter(filters.getId());
			metric.setAggregator(querySql.getAggregator());
			this.metrics.add(metric);
			
			Outputs output = new Outputs();
			output.setId(id);
			this.outputs.add(output);
		}
	}
	
	/**
	 * 构造发送opentsdb参数
	 */
	private QueryExpReqDto createHttpParam() {
		if(EmptyUtils.isEmpty(this.outputs)){
			return null;
		}
		
		Time time = new Time();

		
		Integer interval = queryParam.getInterval();
		if(interval != null && interval > 0){ // 如果interval为null或者<=0 ，那么不采用 downsample
			
			Downsampler downsampler = new Downsampler();
			
			//如果不确定downsampler的aggregator，那么使用和sql表达式一致的aggregator
			if(EmptyUtils.isNotEmpty(queryParam.getIntervalAggregator())){
				downsampler.setAggregator(queryParam.getIntervalAggregator());
			}else{
				downsampler.setAggregator(querySql.getAggregator());
			}
			
			downsampler.setInterval(interval + "s");
			FillPolicy fillPolicy = new FillPolicy();
			fillPolicy.setPolicy(FillPolicyEnum.Null);
			downsampler.setFillPolicy(fillPolicy);
			
			time.setDownsampler(downsampler);
		}
		
		
		time.setStart(queryParam.getBegin());
		time.setEnd(queryParam.getEnd());
		time.setRate(querySql.getRate());
		time.setAggregator(querySql.getAggregator());

		QueryExpReqDto queryExpReqDto = new QueryExpReqDto();
		queryExpReqDto.setTime(time);
		queryExpReqDto.setExpressions(new ArrayList<>());
		queryExpReqDto.setFilters(this.filtersList);
		queryExpReqDto.setMetrics(this.metrics);
		queryExpReqDto.setOutputs(this.outputs);

		return queryExpReqDto;
	}
	
	/**
	 * 转换生成pointMap
	 */
	private List<Map<Long,Double>> converPointMaps(List<List<Double>> dps , Integer seriesSize){
		//防止拿到null，先生成
		List<Map<Long,Double>> pointList = new ArrayList<>(seriesSize);
		for(int i=0;i<seriesSize ;i++){
			pointList.add(new LinkedHashMap<>());
		}
		
		for (int k = 0; k < dps.size(); k++) {
			List<Double> dp = dps.get(k);
			
			long timestamp =converSecond((long)(double)dp.get(0));
			if(timestamp < converSecond(queryParam.getBegin())
					|| timestamp > converSecond(queryParam.getEnd())){
				continue;
			}
			
			for (int j = 0; j < pointList.size(); j++) {
				Double value = dp.get(j+1);//从1开始,0为时间搓
				Map<Long, Double> pointMap = pointList.get(j);//这里上面已经生成过
				pointMap.put(timestamp, value);
			}
		}
		return pointList;
	}
	
	private static final long MAX_SECOND = 9999999999L;
	private long converSecond(long timestamp){
		if(timestamp > MAX_SECOND){
			timestamp /= 1000;
		}
		return timestamp;
	}
	
}
