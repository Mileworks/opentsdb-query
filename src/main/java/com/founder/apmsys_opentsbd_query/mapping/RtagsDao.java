package com.founder.apmsys_opentsbd_query.mapping;

import java.util.List;
import java.util.Map;

import com.founder.apmsys_opentsbd_query.bean.Tag;

public interface RtagsDao {

	/**
	 * 通过userId 获得 k:hostName,v:tags的 Map
	 */
	Map<String, List<Tag>> queryHostAndTagsMapByUserId(String userId);
	
	/**
	 * 通过 userId和hostName,获得tag列表 
	 */
	List<Tag> queryTagsList(String userId,String hostName);
	

	/**
	 * 查询 host 和 tags映射
	 * @param andTags : 必须含有的
	 * @param noTags  : 必须不含有
	 */
	Map<String, List<Tag>> queryHostAndTagsMap(String userId, List<Tag> andTags, List<Tag> noTags);

}
