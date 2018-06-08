package com.founder.apmsys_opentsbd_query.service;

import java.util.List;

import com.founder.apmsys_opentsbd_query.bean.QueryFilter;

/**
 *
 * @author mr.liang
 */
public interface FilterParse {

	/**
	 * 解析合并参数
	 * 
	 * @return
	 * 	如果返回值为null,这条不应该访问opentsdb,存在相斥条件，应该返回空
	 */
	List<List<QueryFilter>> parse();
}
