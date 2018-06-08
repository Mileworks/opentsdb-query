package com.founder.apmsys_opentsbd_query.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class Debug {

	public static final boolean IS_DEBUG = false;
	public static final boolean IS_DEBUG_PRINT = false;
	
	private static ThreadLocal<InfoDto> threadLocal ;
	
	static {
		if(IS_DEBUG){
			threadLocal = new ThreadLocal<>();
		}
	}
	
	
	public static void setQueryRedis(){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setQueryRedis(true);
			if(IS_DEBUG_PRINT){
				System.out.println("查询了redis");
			}
		}
	}
	public static void setOpentsdbResult(Object obj){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setOpentsdbResult(obj);
			if(IS_DEBUG_PRINT){
				System.out.println(JSON.toJSONString(obj,SerializerFeature.WriteMapNullValue));
			}
		}
	}
	
	public static void setResult(Object obj){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setResult(obj);
			if(IS_DEBUG_PRINT){
				System.out.println(JSON.toJSONString(obj,SerializerFeature.WriteMapNullValue));
			}
		}
	}
	
	public static void setHttpParam(Object obj){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setHttpParam(obj);
			if(IS_DEBUG_PRINT){
				System.out.println(JSON.toJSONString(obj,SerializerFeature.WriteMapNullValue));
			}
		}
	}
	
	public static void setQuerySql(String querySql){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setQuerySql(querySql);
			if(IS_DEBUG_PRINT){
				System.out.println(querySql);
			}
		}
	}
	
	public static void setFilterBefore(Object obj){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setFilterBefore(obj);
			if(IS_DEBUG_PRINT){
				System.out.println(JSON.toJSONString(obj,SerializerFeature.WriteMapNullValue));
			}
		}
	}
	
	public static void setFilterAlfer(Object obj){
		if(IS_DEBUG){
			InfoDto infoDto = getDto();
			infoDto.setFilterAfter(obj);
			if(IS_DEBUG_PRINT){
				System.out.println(JSON.toJSONString(obj,SerializerFeature.WriteMapNullValue));
			}
		}
	}
	
	public static InfoDto getAndClear(){
		InfoDto infoDto = getDto();
		clear();
		return infoDto;
	}
	
	public static void clear(){
		if(IS_DEBUG){
			threadLocal.remove();
		}
	}
	
	private static InfoDto getDto(){
		InfoDto infoDto = threadLocal.get();
		if(infoDto == null){
			infoDto = new InfoDto();
			threadLocal.set(infoDto);
		}
		return infoDto;
	}
	
}
