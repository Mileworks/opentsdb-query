package com.founder.apmsys_opentsbd_query.service;

import com.founder.apmsys_opentsbd_query.result.QueryResult;

public interface AccessOpentsdbService {

	/**
	 * 访问opentsdb，转换成需要的参数
	 */
	QueryResult access();
}
