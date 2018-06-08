package com.founder.apmsys_opentsbd_query.mapping;

import java.util.Map;
import java.util.Set;

/**
 * 1.自定义tag 与 host映射 ,避免多次查数据库
 * 2.返回值 必须是string,因为string不可改变
 * 
 * @author mr.liang
 */
public interface TagAndHostMapping {
	
	/**
	 * 拿到全部的自定义tagK,返回tagVAndHosts(Map<TagV,Hosts[]>)
	 */
	public Map<String, Set<String>> queryCustomByCustomTagK(String tagK) ;
	
	
	/**
	 * 通过tagk和tagv 查出相同的tag的 Host[]
	 */
	public Set<String> queryCustomByCustomTagKV(String tagK, String tagV);


	/**
	 * 通过tagK 查相关的主机
	 * @return
	 */
	Set<String> queryHostsByAgentTagK(String agentTagK);
}
