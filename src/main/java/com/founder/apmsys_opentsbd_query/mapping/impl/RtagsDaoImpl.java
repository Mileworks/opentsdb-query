package com.founder.apmsys_opentsbd_query.mapping.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.founder.apmsys_opentsbd_query.bean.Tag;
import com.founder.apmsys_opentsbd_query.mapping.RtagsDao;

public class RtagsDaoImpl implements RtagsDao{
	
	private StringRedisTemplate redisTemplate;
	
	
	public RtagsDaoImpl(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Map<String,List<Tag>> queryHostAndTagsMap(String userId , List<Tag> andTags, List<Tag> noTags) {
		//分组
		List<Tag> andCustomTags   = new ArrayList<>();
		List<Tag> andSysHostTags  = new ArrayList<>();
		List<Tag> andSysAgentTags = new ArrayList<>();
		List<Tag> noCustomTags    = new ArrayList<>();
		List<Tag> noSysHostTags   = new ArrayList<>();
		List<Tag> noSysAgentTags  = new ArrayList<>();
		
		if(andTags != null && andTags.size() > 0){
			for(Tag andTag : andTags){
				if(andTag.isHostTag()){
					andSysHostTags.add(andTag);
				}
				else if(andTag.isAgentTag()){
					andSysAgentTags.add(andTag);
				}
				else{
					andCustomTags.add(andTag);
				}
			}
		}
		
		if(noTags != null && noTags.size() > 0){
			for(Tag noTag : noTags){
				if(noTag.isHostTag()){
					noSysHostTags.add(noTag);
				}
				else if(noTag.isAgentTag()){
					noSysAgentTags.add(noTag);
				}
				else{
					noCustomTags.add(noTag);
				}
			}
		}
		
		Map<String,List<Tag>> hostAndTagsMap = queryHostAndTagsMapByUserId(userId);
		
		//过滤出可以用的tag
		Map<String,List<Tag>> filterHostAndTagsMap0 = new HashMap<>();
		
		f0:for(Entry<String,List<Tag>> entry : hostAndTagsMap.entrySet()){
			String    host = entry.getKey();
			List<Tag> tags = entry.getValue();
			
			
			//这要有一个有tag包含，那么就不要
			for(Tag tag: tags){
				
				//NOT tag
				if(tag.isAgentTag()){//agent
					String agent = tag.getTagV();
					for(Tag noSysAgentTag : noSysAgentTags){
						
						String noAgent = noSysAgentTag.getTagV();
						if(checkRepeatAgentTagV(agent,noAgent)){//如果存在no tag 那么不要
							continue f0;
						}
					}
				} else {//只有自定义tag(#,@agent),不存在其他tag
					
					for(Tag noCustomTag : noCustomTags){
						if(noCustomTag.equals(tag)){//如果存在no tag 那么不要
							continue f0;
						}
					}
				}
			}
			
			for(Tag andSysAgent : andSysAgentTags){
				String addAgent = andSysAgent.getTagV();
				
				boolean checkContain = false;
				for(Tag tag: tags){
					String agent = tag.getTagV();
					if(checkRepeatAgentTagV(agent,addAgent)){//是否存在重复
						checkContain = true;
						break;
					}
				}
				//这里存在两种情况 
				//1.tags.size() == 0
				//2.tags里面不包含 andSysAgentTags
				if(!checkContain){
					continue f0;
				}
			}
			
			
			for(Tag andCustomTag : andCustomTags){
				
				boolean checkContain = false;
				for(Tag tag: tags){
					if(andCustomTag.equals(tag)){//是否存在重复
						checkContain = true;
						break ;
					}
				}
				//这里存在两种情况 
				//1.tags.size() == 0
				//2.tags里面不包含 andSysAgentTags
				if(!checkContain){
					continue f0;
				}
			}
			
			filterHostAndTagsMap0.put(host, tags);
		}
		
		return filterHostAndTagsMap0;
	}
	
	@Override
	public List<Tag> queryTagsList(String userId, String hostName) {
		return queryHostAndTagsMapByUserId(userId).get(hostName);
	}

	@Override
	public Map<String,List<Tag>> queryHostAndTagsMapByUserId(String userId) {

		String fixedSerachKey = "search:hosts:".concat(userId).concat(":");
		
		/**
		 * keys 和 keysMap 是一一对应的
		 * keys:[123,456]
		 * keyMap:[{},{a=1}]
		 */
		List<String> keys = new ArrayList<>(redisTemplate.keys(fixedSerachKey.concat("*")));
		
		List<Object> keysMapList = redisTemplate.executePipelined(new SessionCallback<Object>() {
			@Override @SuppressWarnings({ "rawtypes", "unchecked" })
			public Object execute(RedisOperations operations) throws DataAccessException {
				for(String key:keys){
					operations.boundHashOps(key).entries();
				}
				return null;
			}
		});

		/**
		 * 按照host分组,tags按照时间排序 升序
		 */
		Map<String,List<Tag>> hostAndTagsMap = new HashMap<>();
		
		for(int i=0;i<keysMapList.size();i++){
			
			Map<String,String> hostTimeMap = (Map<String,String>) keysMapList.get(i);
			
			String[] tagArr = keys.get(i).substring(fixedSerachKey.length()).split("=");
			
			for(Entry<String, String> entry:hostTimeMap.entrySet()){
				String hostName = entry.getKey();
				Long   time     = Long.valueOf(entry.getValue());
				
				Tag tag = new Tag();
				tag.setTagK(tagArr[0]);
				tag.setTagV(tagArr[1]);
				tag.setTime(time);
				
				List<Tag>tags = hostAndTagsMap.get(hostName);
				if(tags == null){
					tags = new ArrayList<>();
					hostAndTagsMap.put(hostName, tags);
				}
				
				insertTagOrderAsce(tag,tags);
			}
		}
		return hostAndTagsMap;
	}
	
	private boolean checkRepeatAgentTagV(String agentTagV0,String agentTagV1){
		String[] agentTagV0TagVArr = agentTagV0.split(",");
		String[] agentTagV1TagVArr = agentTagV1.split(",");
		
		for(String agentTagV0TagV : agentTagV0TagVArr){
			for(String agentTagV1TagV : agentTagV1TagVArr){
				if(agentTagV0TagV.equals(agentTagV1TagV)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * list必须是升序，否则错误，建议用 空的list 循环插Tag
	 */
	private final void insertTagOrderAsce(Tag tag,List<Tag> list){
		insertTagOrderAsce(tag,list,0,list.size());
	}
	
	/**
	 * 二分查找插入
	 */
	private final void insertTagOrderAsce(Tag tag,List<Tag> list,Integer startIndex,Integer endIndex){
		int rangeIndex = endIndex - startIndex;
		if(rangeIndex == 0 ){
			list.add(endIndex,tag);
			return ;
		}
		
		int compareIndex = rangeIndex / 2;
		int realIndex 	 = startIndex + compareIndex;
		
		Long time = tag.getTime();
		Long compareTime = list.get(realIndex).getTime();
		if(time > compareTime){
			if(rangeIndex == 1){
				list.add(realIndex + 1,tag);
			}else{
				this.insertTagOrderAsce(tag,list,realIndex,endIndex);
			}
		}
		else if(time < compareTime){
			if(rangeIndex == 1){
				list.add(realIndex,tag);
			}else{
				this.insertTagOrderAsce(tag,list,startIndex,realIndex);
			}
		}
		else {
			list.add(realIndex,tag);
		}
	}

}