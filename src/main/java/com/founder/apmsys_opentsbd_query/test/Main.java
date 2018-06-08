package com.founder.apmsys_opentsbd_query.test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.founder.apmsys_opentsbd_query.param.QueryBatchParam;
import com.founder.apmsys_opentsbd_query.service.BatchQueryService;
import com.google.gson.GsonBuilder;

public class Main {
	
	static BatchQueryService batchQuery;

	static String userId = "28";
	
	
	public static void main(String[] args) throws IOException {
		ApplicationContext ac = new ClassPathXmlApplicationContext(
				new String[]{"classpath:apmsys-servlet.xml"});
		
		batchQuery= ac.getBean(BatchQueryService.class);
		List<String> qs =  getqs();
		
		List<InfoDto> infoList = new ArrayList<>();
		int i=0;
		for(String q : qs){
			infoList.add(testOne(q,i++));
		}
		String json = new GsonBuilder().serializeNulls().create().toJson(infoList);
//		String json = JSON.toJSONString(infoList);
		
//		System.out.println(JSON.toJSONString(infoList.get(0).getFilterBefore()));
		
		json = "DATA_ARR = ".concat(json);
		PrintWriter pw  = new PrintWriter(new FileOutputStream("src/main/java/com/founder/monitor/bean/batchQueryV4/test/show/data"));
		pw.print(json);
		pw.close();
	}
	
	static List<String> getqs() throws IOException{
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		System.out.println(Main.class.getResource("testQuerySql.properties"));
		InputStream is = Main.class.getResourceAsStream("testQuerySql.properties");
		
		byte[] readBytes = new byte[1024];
		
		int len = 0;
		while((len = is.read(readBytes))>0){
			outSteam.write(readBytes,0,len);
		}
		outSteam.close();
		is.close();
		String qs = new String(outSteam.toByteArray());
		
		return JSON.parseArray(qs, String.class);
	}
	
	static InfoDto testOne(String q,int i){
		Map<String,Object> map = new HashMap<>();
		map.put("index", i);
		
		QueryBatchParam queryParam = new QueryBatchParam();
		queryParam.setEnd(new Date().getTime() / 1000);
		queryParam.setBegin(queryParam.getEnd() - 3600);
		queryParam.setAttributes(map);
		queryParam.setInterval(60);
		queryParam.setQ(q);
		
		batchQuery.singleQuery(queryParam, userId);
		
		return Debug.getAndClear();
	}
}
