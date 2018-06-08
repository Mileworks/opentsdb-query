package com.founder.apmsys_opentsbd_query.service;

import java.util.List;

import com.founder.apmsys_opentsbd_query.bean.GroupFilter;
import com.founder.apmsys_opentsbd_query.bean.QueryFilter;

/**
 * 将queryFilter合并
 * 
 *
 * @author mr.liang
 */
public interface FilterGroupMeger {

	/**
	 * 合并
	 */
	List<GroupFilter> meger(List<List<QueryFilter>> groupFilter);
}
