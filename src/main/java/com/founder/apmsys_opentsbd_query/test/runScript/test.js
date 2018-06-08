const fs = require('fs');
const http = require('http');
 

let test_metric  = 'test1.test.1';
let apiKey     	 = '49fec1de888146f1904118fea9d67fd8';
let uid          = '28';

let startPackge1Data = JSON.parse(fs.readFileSync('startPackge1.json').toString());
let generalPackge    = JSON.parse(fs.readFileSync('generalPackge.json').toString());

let host_tag = {
	"101":{
		"device":["c","d","e"],
		
		"#address":"wh",
		"#level":"1"
	},
	"102":{
		"device":["c","d"],
		"@agent" :["win8","IDC_1"],
		
		"#address":"bj",
		"#level":"2"
	},
	"103":{
		"device":["c","e"],
		"@agent" :["win7"]
		
	},
	"104":{
		"device":["d","e"],
		"@agent":["win8","IDC_2"],
		
		"#address":"bj"
	},
	"105":{
		"device":["c"],
		"@agent":["win7","IDC_1"]
	},
	"106":{
		"device":["d"],
		"@agent":["win8"],
		
		"#level":"2"
	},
	"107":{
		"device":["d"],
		"@agent":["win7","IDC_2"],
		
		"#level":"1"
	},
	"108":{ 
		"#address":"bj",
		"#level":"2"
	},
	"109":{ 
		"device":["c"],
		"@agent":["win7","IDC_1"],
		
		"#address":"wh",
		"#level":"1"
	},
	"110":{ 
		
	}
};


let intake = {
	hostname:'172.29.231.177',
	port:'10000',
	path:'/intake/?api_key='+apiKey,
	method:'POST',
	headers: {
        'User-Agent': 'Datadog Agent',
        'Content-Type': 'application/json'
    }
};


let openTag = {
	hostname:'172.29.231.177',
	port:'8101',
	path:'/api/host/tag?uid='+uid+'&host=',
	method:'POST',
	headers: {
		'User-Agent': 'Datadog Agent',
        'Content-Type': 'application/json'
	}
};

{
	let dataArr = changeData(startPackge1Data);
	dataArr.forEach((data)=>{
		sendMetric(data);
	});
	
	//
	sendTag();
	
	setInterval( ()=>{
		let dataArr = changeData(generalPackge); 
		dataArr.forEach((data)=>{
			sendMetric(data);
		});
	},1000 * 15);
}

function sendMetric(data){
	sendHttp(intake,data);
}


function sendTag(){
	for(let host in host_tag){
		let postOption = extend({},openTag);
		
		postOption.path = postOption.path+host;
		let tags = host_tag[host];
		
		let jsonContent = [];
		
		if(tags["#level"]){
			jsonContent.push({"#level":tags["#level"]});
		}
		if(tags["#address"]){
			jsonContent.push({"#address":tags["#address"]});
		}
		sendHttp(postOption,jsonContent);
	}
}

function sendHttp(options,data){
	//fs.appendFileSync("./log.log",JSON.stringify(data));
	//console.log(JSON.stringify(data));
	//return data;
	
	var postData = JSON.stringify(data);
	
	var req = http.request(options, (res) => {
	  console.log(`STATUS: ${res.statusCode}`);
	  console.log(`HEADERS: ${JSON.stringify(res.headers)}`);
	  res.setEncoding('utf8');
	  res.on('data', (chunk) => {
		console.log(`主体: ${chunk}`);
	  });
	  res.on('end', () => {
		console.log('响应中已无数据。');
	  });
	});

	req.on('error', (e) => {
	  console.log(`请求遇到问题: ${e.message}`);
	});

	// 写入数据到请求主体
	req.write(postData);
	req.end();
}



//核心测试业务
function changeData(jsonData){
	let rDataArr = [];
	
	let timestamp = new Date().getTime() / 1000;
	for(let host in host_tag){
	   let tag 		= host_tag[host],
	   
		   device 	= tag.device,
		   agent	= tag["@agent"],
		   data     = extend({},jsonData);
		  
//console.log(jsonData.metrics);		  
		//console.log(jsonData.toString());
				//console.log(device);
	   if(device){
		   //console.log(device);
		   
		   device.forEach( (temp)=> {
			   
			   let templateArr =  [
					test_metric,
					timestamp,
					1,
					{
						"hostname":host,
						"type":"gauge",
						"device_name":temp
					}
				];
				
				data.metrics.push(templateArr);
		   });
	   }
	   
	   if(agent){
		  let host_tags = { "system":agent };
		  data["host-tags"] = host_tags;
	   }
	   
	   replaceVal(data,{
		   "apiKey":apiKey,
		   "api_key":apiKey,
		   
		   "host":host,
		   "hostname":host,
		   "host_name":host,
		   "socket-hostname":host,
		   "internalHostname":host,
		   "socket-fqdn":host,
		   
		   "timestamp":timestamp,
		   "collection_timestamp":timestamp
		   
	   });
	   
	   rDataArr.push(data);
   }
   return rDataArr;
}


// 1.替换key相同的
// 2.replaceObj,和被替换的value 必须是键值对
function replaceVal(obj,replaceObj){
	
   //all host
   a:for(let key in obj){
	   let value = obj[key];

		//必须是对象才能覆盖
		if (   typeof value =='object'//原生判断比较快
			&& Object.prototype.toString.call(value) == '[object Object]'
		) {
			replaceVal(value,replaceObj);
		}
		else if(Array.isArray(value)){
			value.forEach( (val)=> {
				if(typeof val == 'object'){
					replaceVal(val,replaceObj);
				}	
			});
		}	
		else{
			
			for(let rKey in replaceObj){
				let rValue = replaceObj[rKey];
				if(rKey == key){
					obj[key] = rValue;
					continue a;
				}
			}
			
		}
   }
}



/**
 * 描述 : 后面覆盖前面第一个
 *
 * extend({},option) //对 option深克隆
 *
 * @author mr.liang
 */
function extend(option0={},...options) {
	if (!options || options.length == 0) {
		return option0;
	}

	options.forEach((option={})=> {
		//
		Object.keys(option).forEach((key)=> {
			let  value = option[key],
				 oldValue = option0[key];

			//必须是对象才能覆盖
			if (   typeof value =='object'//原生判断比较快
				&& Object.prototype.toString.call(value) == '[object Object]'
			) {

				if(Object.prototype.toString.call(oldValue) == '[object Object]'){
					value = extend(oldValue,value);
				}
				else if(Array.isArray(oldValue)){
					value = extend([],oldValue);
				}
				else{
					//如果是非对象直接覆盖
					value = extend({},value);
				}
			}
			else if(Array.isArray(value)){
				let _value = value;
				value = [];//覆盖之前的
				
				if(Array.isArray(oldValue)){//两个都是数组，那么后一个覆盖前面一个
					
					oldValue.forEach( (oval)=>{
						if(typeof oval =='object'){
							value.push( extend({},oval) );
						}else if(Array.isArray(oval)){
							value.push( extend([],oval) );
						}else{
							value.push(oval);
						}
					});
				}
				else{
					
					_value.forEach( (val)=>{
						if(typeof val =='object'){
							value.push( extend(oldValue,val) );
						}else if(Array.isArray(val)){
							value.push( extend([],val) );
						}else{
							value.push(val);
						}
					});
				}
				
				
			}
			option0[key] = value;
		});
		
	});

	return option0;
}