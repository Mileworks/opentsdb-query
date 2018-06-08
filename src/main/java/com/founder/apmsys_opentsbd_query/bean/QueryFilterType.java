package com.founder.apmsys_opentsbd_query.bean;

/**
 * | 或
 * ! 非
 * * 全部
 * @author mr.liang
 */
public enum QueryFilterType {
	//|
	OR,
	
	//! |
	NOT_OR,
	
	//!*
	NOT_ALL,
	
	//*,这种情况目前来说仅会出现在metric打的tag上面
	SYS_ALL;
}
