package com.founder.apmsys_opentsbd_query.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.founder.apmsys_opentsbd_query.param.QueryBatchParams;
import com.founder.apmsys_opentsbd_query.service.BatchQueryService;

public class Test {
	static BatchQueryService batchQuery;
	
	public static void main(String[] args) throws IOException {
		ApplicationContext ac = new ClassPathXmlApplicationContext(
				new String[]{"classpath:apmsys-servlet.xml"});
		
		batchQuery= ac.getBean(BatchQueryService.class);
		
		QueryBatchParams queryBatchParams = get();
		queryBatchParams.setUserId("1");
		
		System.out.println(batchQuery.batchQuery(queryBatchParams));
		
	}
	
	static QueryBatchParams get() throws IOException{
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		InputStream is = Main.class.getResourceAsStream("test.text");
		
		byte[] readBytes = new byte[1024];
		
		int len = 0;
		while((len = is.read(readBytes))>0){
			outSteam.write(readBytes,0,len);
		}
		outSteam.close();
		is.close();
		String qs = new String(outSteam.toByteArray());
		
		return JSON.parseObject(qs, QueryBatchParams.class);
	}
}
