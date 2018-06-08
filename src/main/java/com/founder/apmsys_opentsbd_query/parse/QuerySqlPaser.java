package com.founder.apmsys_opentsbd_query.parse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.founder.apmsys_opentsbd_query.bean.Tag;
import com.founder.apmsys_opentsbd_query.utils.EmptyUtils;

/**
 * 
 * @author mr.liang
 */
public class QuerySqlPaser {

	private static final String REGEXP = 
			  "(?<aggregator>avg|max|min|sum)"
			+ "(:(?<rate>rate))?"
			+ "(:(?<metric>[^\\{]+))"
			+ "((\\{(?<tags>[^\\}]*)\\})(by\\{(?<groupby>[^\\}]*)\\})?)?";
	
	public static QuerySqlPaserResult parse(String querySql){
		QuerySqlPaserResult result = new QuerySqlPaserResult();
		
		result.querySql = querySql;
		
		Matcher m = Pattern.compile(REGEXP).matcher(result.querySql);
		m.find();
		
		result.aggregator = m.group("aggregator");
		result.metric 	 = m.group("metric");
		result.rate 	 = EmptyUtils.isNotEmpty ( m.group("rate") ) ? true:false;
		
		//======================================================================================
		String groupby = m.group("groupby");
		String tags = m.group("tags");
		
		//bySet cusBySet sysBySet
		if(EmptyUtils.isNotEmpty(groupby)){
			parseBy(result,groupby);
		}
		
		//fromTagMap cusFromTagMap sysFromTagMap noTagMap isSelectAll
		if(EmptyUtils.isNotEmpty(tags)){
			parseTag(tags,result);
		}
		
		//区分cusFromTag和sysFromTags , cusNoTagMap和sysNoTagMap , cusBySet sysBySet
		differentiateCustomAndSystem(result);

		//
		differentiateRepateAndUnique(result);
		
		return result;
	}
	
	private static void parseBy(QuerySqlPaserResult querySql,String groupby){
		if(EmptyUtils.isNotEmpty(groupby)){
			
			String[] groupBys = groupby.trim().split(",");
			for (String by : groupBys) {
				if (EmptyUtils.isEmpty(querySql.bySet)) {
					querySql.bySet = new HashSet<>();
				}
				querySql.bySet.add(by);
			}
		}
	}
	
	private static void parseTag(String tags,QuerySqlPaserResult querySql){
		if(EmptyUtils.isNotEmpty(tags)){
			String[] tagArr = tags.trim().split(",");
			
			//对tag定性
			for(String tag : tagArr){
				
				tag = tag.trim();
				
				if("*".equals(tag)){//查询全部 不算tag
					querySql.isSelectAll = true;
					return;
				}
				
				//=================================filter用到=============
				if(tag.indexOf(":") < 0){
					//win7
					if(tag.indexOf("!") < 0){
						if(querySql.cusAgentTags == null){
							querySql.cusAgentTags = new HashSet<>();
						}
						querySql.cusAgentTags.add(tag);
					}
					//!win7
					else{
						if(querySql.cusNoAgentTags == null){
							querySql.cusNoAgentTags = new HashSet<>();
						}
						querySql.cusNoAgentTags.add(tag.substring(1));
					}
					
				}else{
					String[] 				tagKV  = null;
					Map<String,Set<String>> tagMap = null;
					
					//tagK:tagV
					if(tag.indexOf("!") < 0){
						tagKV = tag.split(":");
						if(querySql.fromTagMap == null){
							querySql.fromTagMap = new HashMap<>();
						}
						tagMap = querySql.fromTagMap;
					}
					//!tagK:tagV
					else {
						tagKV = tag.substring(1).split(":");
						if(querySql.noTagMap == null){
							querySql.noTagMap = new HashMap<>();
						}
						tagMap = querySql.noTagMap;
					}
					
					String tagK = tagKV[0];
					String tagV = tagKV[1];
					
					Set<String> tagVSet  = tagMap.get(tagK);
					if(tagVSet == null){
						tagVSet = new HashSet<>();
						tagMap.put(tagK, tagVSet);
					}
					tagVSet.add(tagV);
				}
			}
		}
	}
	
	private static void differentiateCustomAndSystem(QuerySqlPaserResult querySql){
		
		//先区分by
		Set<String> bySet = querySql.bySet;
		if(EmptyUtils.isNotEmpty(bySet)){
			for(String by : querySql.bySet){
				//by里面不存在agentTag，原因是 无法知道 from{}by{table}，这个tabel是探针tag还是自定义tag
				if (Tag.isCustomTag(by)) {
					if (querySql.cusBySet == null) {
						querySql.cusBySet = new HashSet<>();
					}
					querySql.cusBySet.add(by);
				}
				else {
					if (querySql.sysBySet == null) {
						querySql.sysBySet = new HashSet<>();
					}
					querySql.sysBySet.add(by);
				}
			}
		}
		
		if(querySql.isSelectAll){//查询全部 不分tag
			return ;
		}
		
		Map<String, Set<String>> fromTagMap = querySql.fromTagMap;
		if(EmptyUtils.isNotEmpty(fromTagMap)){
			for(Entry<String, Set<String>> entry : fromTagMap.entrySet()){
				String 		tagK  = entry.getKey();
				Set<String> tagVs = entry.getValue();
				
				if(Tag.isCustomTag(tagK)){
					if(querySql.cusFromTagMap == null){
						querySql.cusFromTagMap = new HashMap<>();
					}
					querySql.cusFromTagMap.put(tagK, tagVs);
				}else{
					if(querySql.sysFromTagMap == null){
						querySql.sysFromTagMap = new HashMap<>();
					}
					querySql.sysFromTagMap.put(tagK, tagVs);
				}
			}
		}
		
		Map<String, Set<String>> noTagMap = querySql.noTagMap;
		if(EmptyUtils.isNotEmpty(noTagMap)){
			for(Entry<String, Set<String>> entry : noTagMap.entrySet()){
				String 		tagK  = entry.getKey();
				Set<String> tagVs = entry.getValue();
				
				if(Tag.isCustomTag(tagK)){
					if(querySql.cusNoTagMap == null){
						querySql.cusNoTagMap = new HashMap<>();
					}
					querySql.cusNoTagMap.put(tagK, tagVs);
				}else{
					if(querySql.sysNoTagMap == null){
						querySql.sysNoTagMap = new HashMap<>();
					}
					querySql.sysNoTagMap.put(tagK, tagVs);
				}
			}
		}
		
		
	}
	
	private static void differentiateRepateAndUnique(QuerySqlPaserResult querySql){
		
		//repeatCusFromTagMap uniqueCusFromTagMap
		if(EmptyUtils.isNotEmpty(querySql.cusFromTagMap)){
			a:for(Entry<String, Set<String>> entry : querySql.cusFromTagMap.entrySet()){
				
				String 		tagK  = entry.getKey();
				Set<String> tagVs = entry.getValue();
				
				if(querySql.cusBySet != null){
					for(String cusBy : querySql.cusBySet){
						if(cusBy.equals(tagK)){
							if(querySql.repeatCusFromTagMap == null){
								querySql.repeatCusFromTagMap = new HashMap<>();
							}
							//address:wh,address:bj,address:tj
							//from{address:wh}by{address:wh} 	==>!address:wh
							//from{}by{address:wh}				==>!address:* 
							querySql.repeatCusFromTagMap.put(tagK, tagVs);
							continue a;
						}
					}
				}
				
				if(querySql.uniqueCusFromTagMap == null){
					querySql.uniqueCusFromTagMap = new HashMap<>();
				}
				querySql.uniqueCusFromTagMap.put(tagK, tagVs);
			}
		}
		
		//uniqueCusBySet
		if(EmptyUtils.isNotEmpty(querySql.cusBySet)){
			
			a:for(String customBy:querySql.cusBySet){
				if(!EmptyUtils.isEmpty(querySql.cusFromTagMap)){
					for(Entry<String, Set<String>> customFromTagEntry:querySql.cusFromTagMap.entrySet()){
						if(customFromTagEntry.getKey().equals(customBy)){
							continue a;
						}
					}
				}
				if(querySql.uniqueCusBySet == null){
					querySql.uniqueCusBySet = new HashSet<>();
				}
				querySql.uniqueCusBySet.add(customBy);
			}
		}
		
		//repeatSysFromTagMap uniqueSysFromTagMap
		if(EmptyUtils.isNotEmpty(querySql.sysFromTagMap)){
			
			a:for(Entry<String, Set<String>> sysFromTagEntry:querySql.sysFromTagMap.entrySet()){
				String 		tagK  = sysFromTagEntry.getKey();
				Set<String> tagVs = sysFromTagEntry.getValue();
				
				if(EmptyUtils.isNotEmpty(querySql.sysBySet)){
					for(String sysBy:querySql.sysBySet){
						
						if(tagK.equals(sysBy)){
							if(querySql.repeatSysFromTagMap == null){
								querySql.repeatSysFromTagMap = new HashMap<>();
							}
							querySql.repeatSysFromTagMap.put(tagK, tagVs);
							continue a;
						}
					}
				}
				if(querySql.uniqueSysFromTagMap == null){
					querySql.uniqueSysFromTagMap = new HashMap<>();
				}
				querySql.uniqueSysFromTagMap.put(tagK, tagVs);
			}
		}
		
		//uniqueSysBySet
		if(EmptyUtils.isNotEmpty(querySql.sysBySet)){
			
			a:for(String sysBy:querySql.sysBySet){
				
				if(!EmptyUtils.isEmpty(querySql.sysFromTagMap)){
					for(Entry<String, Set<String>> sysFromTagEntry:querySql.sysFromTagMap.entrySet()){
						if(sysFromTagEntry.getKey().equals(sysBy)){
							continue a;
						}
					}
				}
				if(querySql.uniqueSysBySet == null){
					querySql.uniqueSysBySet = new HashSet<>();
				}
				querySql.uniqueSysBySet.add(sysBy);
			}
		}
	}
	
	public static void main(String[] args) {
		String query = 
			"avg:rate:system.cpu.idle{host:101,device:e,#address:wh,#level:1,win7,!win8,!#address:sh}by{device,#level}";
		QuerySqlPaserResult queryResult = parse(query);
		System.out.println(JSON.toJSONString(queryResult));
	}
	
	
	

	/**
	 * unique 和 repeat 是相对解析类而言
	 * 
	 * @author mr.liang
	 */
	public static class QuerySqlPaserResult {
		
		//=================================基础参数========================================
		private String  metric;
		private String  aggregator;
		private Boolean rate;
		private String  querySql;
		
		//================================状态值=====================================
		private boolean isSelectAll;
		
		//=================================中间量========================================
		private Map<String,Set<String>> fromTagMap; 
		private Map<String,Set<String>> sysFromTagMap; 
		private Map<String,Set<String>> cusFromTagMap;
		
		private Set<String> bySet;
		private Set<String> cusBySet;
		private Set<String> sysBySet;
		
		private Map<String,Set<String>> noTagMap;
		
		//=================================filter用到=======================================
		private Map<String,Set<String>> uniqueCusFromTagMap ;
		private Map<String,Set<String>> repeatCusFromTagMap ;
		private Map<String,Set<String>> uniqueSysFromTagMap ;
		private Map<String,Set<String>> repeatSysFromTagMap ;
		
		private Set<String> uniqueCusBySet ;
		private Set<String> uniqueSysBySet ;
		
		private Map<String,Set<String>> sysNoTagMap ;
		private Map<String,Set<String>> cusNoTagMap ;
		
		private Set<String> 			cusAgentTags;
		private Set<String> 			cusNoAgentTags;
		//=================================filter用到=======================================
		
		private QuerySqlPaserResult(){}
		
		/**
		 * 是否包含 自定义的tag
		 */
		public boolean isContainCustomTag(){
			if(EmptyUtils.isNotEmpty(cusFromTagMap) 
					|| EmptyUtils.isNotEmpty(cusBySet) 
					|| EmptyUtils.isNotEmpty(cusNoTagMap)
					|| EmptyUtils.isNotEmpty(cusAgentTags)
					|| EmptyUtils.isNotEmpty(cusNoAgentTags)){
				return true;
			}
			return false;
		}
		
		public Map<String, Set<String>> getCusFromTagMap() {
			return cusFromTagMap;
		}
		public Map<String, Set<String>> getSysFromTagMap() {
			return sysFromTagMap;
		}
		public String getMetric() {
			return metric;
		}
		public String getAggregator() {
			return aggregator;
		}
		public Boolean getRate() {
			return rate;
		}
		public Set<String> getBySet() {
			return bySet;
		}
		public Map<String, Set<String>> getUniqueCusFromTagMap() {
			return uniqueCusFromTagMap;
		}
		public Map<String, Set<String>> getRepeatCusFromTagMap() {
			return repeatCusFromTagMap;
		}
		public Map<String, Set<String>> getUniqueSysFromTagMap() {
			return uniqueSysFromTagMap;
		}
		public Map<String, Set<String>> getRepeatSysFromTagMap() {
			return repeatSysFromTagMap;
		}
		public Set<String> getUniqueCusBySet() {
			return uniqueCusBySet;
		}
		public Set<String> getUniqueSysBySet() {
			return uniqueSysBySet;
		}
		public Map<String, Set<String>> getSysNoTagMap() {
			return sysNoTagMap;
		}
		public Map<String, Set<String>> getCusNoTagMap() {
			return cusNoTagMap;
		}
		public String getQuerySql() {
			return querySql;
		}
		public Set<String> getCusAgentTags() {
			return cusAgentTags;
		}
		public Set<String> getCusNoAgentTags() {
			return cusNoAgentTags;
		}
	}
	
	
}
