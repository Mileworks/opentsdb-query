package com.founder.apmsys_opentsbd_query.param;

import java.util.List;

/**
 * 
 * @author mr.liang
 */
public class QueryBatchParams {

	private List<QueryBatchParam> queries;

	private String userId;

	public List<QueryBatchParam> getQueries() {
		return queries;
	}

	public void setQueries(List<QueryBatchParam> queries) {
		this.queries = queries;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}