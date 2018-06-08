package com.founder.apmsys_opentsbd_query.mapping;

import com.founder.apmsys_opentsbd_query.mapping.impl.TagAndHostMappingImpl;

public class TagAndHostMappingFactory {

	private RtagsDao rtagsDao;
	
	public TagAndHostMappingFactory(RtagsDao rtagsDao) {
		this.rtagsDao = rtagsDao;
	}

	public TagAndHostMapping getTagAndHostMapping(String userId){
		return new TagAndHostMappingImpl(userId, rtagsDao.queryHostAndTagsMapByUserId(userId));
	}
}
