package com.founder.apmsys_opentsbd_query.utils;

import java.util.Collection;
import java.util.Map;

public class EmptyUtils {

	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(Map map){
		return !isEmpty(map);
	}
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Map map){
		if(map==null || map.isEmpty()){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(Collection collection){
		return !isEmpty(collection);
	}
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Collection collection){
		if(collection==null || collection.isEmpty()){
			return true;
		}
		return false;
	}
	
	public static boolean isNotEmpty(String str){
		return !isEmpty(str);
	}
	public static boolean isEmpty(String str){
		if(str==null || str.isEmpty()){
			return true;
		}
		return false;
	}
}
