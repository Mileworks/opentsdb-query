package com.founder.apmsys_opentsbd_query.service;

import java.util.List;

import com.founder.apmsys_opentsbd_query.param.QueryBatchParam;
import com.founder.apmsys_opentsbd_query.param.QueryBatchParams;
import com.founder.apmsys_opentsbd_query.result.QueryResult;

/**
 * <ul>
 * 	<li>
 * 		1.原则问题:
 * 		<ul>
 * 			<li>1. uid 和 host 关键字, uid 代表userId，host 代表 主机。</li>
 * 			<li>2. 自定义tag，一个host里面 相同的tagK，仅能出现一个tagV 。</li>
 * 			<li>3. 以下例子最后必须返回 pass-70
 * 				<code>
 * 					{
 *	                	"type":"literal_or",
 *	                    "tagk":"host",
 *	                    "filter":"paas-178|paas-70",
 *	                    "groupBy":false
 *	                },
 *	                {
 *	                	"type":"not_literal_or",
 *	                	"tagk":"host",
 *	                    "filter":"paas-178",
 *	                    "groupBy":false
 *	                },
 *	                {
 *	                    "type":"wildcard",
 *	                    "tagk":"host",
 *	                    "filter":"*",
 *	                    "groupBy":true
 *	                }
 * 				</code>
 * 			</li>
 * 		</ul>
 * 	</li>
 * </ul>
 * @author mr.liang
 */
public interface BatchQueryService {

	/**
	 * 多次查询
	 */
	List<QueryResult> batchQuery(QueryBatchParams queryBatchParams);
	
	/**
	 * 单次查询
	 */
	QueryResult singleQuery(QueryBatchParam queryParam, String userId);

}
