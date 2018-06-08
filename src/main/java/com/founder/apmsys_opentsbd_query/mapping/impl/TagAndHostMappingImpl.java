package com.founder.apmsys_opentsbd_query.mapping.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.founder.apmsys_opentsbd_query.bean.Tag;
import com.founder.apmsys_opentsbd_query.mapping.TagAndHostMapping;
import com.founder.apmsys_opentsbd_query.utils.EmptyUtils;

public class TagAndHostMappingImpl implements TagAndHostMapping{
	
	private Map<String, List<Tag>> hostAndTagsMap;
	
	private String userId ;
	
	public TagAndHostMappingImpl(String userId,Map<String, List<Tag>> hostAndTagsMap) {
		this.userId = userId;
		this.hostAndTagsMap = hostAndTagsMap;
	}
	
	/**
	 * 对 @ agent的一种特殊处理
	 * @return
	 */
	@Override
	public Set<String> queryHostsByAgentTagK(String agentTagK){
		
		Set<String> hosts = new HashSet<>();
		for (Entry<String, List<Tag>> entry : hostAndTagsMap.entrySet()) {
			String 	  host = entry.getKey();
			List<Tag> tags = entry.getValue();
			
			a:for(Tag tag : tags){
				if(tag.isAgentTag()){
					String[] tagKs = tag.getAgentTagKs();
					
					for(String tagK : tagKs){
						if(tagK.equals(agentTagK)){
							hosts.add(host);
							continue a;
						}
					}
				}
			}
		}
		return hosts;
	}
	
	
	
	/**
	 * 拿到全部的自定义tagK,返回tagVAndHosts(Map<TagV,Hosts[]>)
	 * 
	 * 如果是@agent
	 */
	public Map<String, Set<String>> queryCustomByCustomTagK(String tagK) {
		if (EmptyUtils.isEmpty(tagK)) {
			return null;
		}

		Map<String, Set<String>> tagVAndHosts = new HashMap<>();

		for (Entry<String, List<Tag>> entry : hostAndTagsMap.entrySet()) {
			String host = entry.getKey();

			for (Tag tag : entry.getValue()) {
				String tempTagK = tag.getTagK();
				String tempTagV = tag.getTagV();

				if (tagK.equals(tempTagK)) {
					Set<String> hosts = tagVAndHosts.get(tempTagV);
					if (hosts == null) {
						hosts = new HashSet<>();
						tagVAndHosts.put(tempTagV, hosts);
					}
					hosts.add(host);
				}
			}
		}

		return tagVAndHosts;
	}

	/**
	 * 通过tagk和tagv 查出相同的tag的 Host[]
	 */
	public Set<String> queryCustomByCustomTagKV(String tagK, String tagV) {
		Set<String> hosts = new HashSet<>();

		Tag compareTag = new Tag();
		compareTag.setTagK(tagK);
		compareTag.setTagV(tagV);

		for (Entry<String, List<Tag>> entry : hostAndTagsMap.entrySet()) {
			String host = entry.getKey();

			for (Tag tag : entry.getValue()) {
				if (compareTag.equals(tag)) {
					hosts.add(host);
				}
			}
		}
		return hosts;
	}

	
}
