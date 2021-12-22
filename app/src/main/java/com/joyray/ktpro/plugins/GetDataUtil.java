package com.joyray.ktpro.plugins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

public class GetDataUtil {
	public static String USER_AGENT = "User-Agent";
	public static String USER_AGENT_VALUE = "Mozilla/5.0 (Linux NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36";
	public static JSONObject getUrlParmas(String query) {
		if(query.indexOf("?")>=0) {
			query = query.substring(query.indexOf("?")+1);
		}
		System.out.println("query:"+query);
    	String parms[] = query.split("&");
    	JSONObject result = new JSONObject();
    	if(parms.length>0) {
    		for(int i=0;i<parms.length;i++) {
    			String kvs[] = parms[i].split("=");
    			if(kvs.length==2) {
    				result.put(kvs[0], kvs[1]);
    			}
    		}
    	}
    	return result;
    }
	
	
	public static JSONObject getHttpGetJsonDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies) {
		return getHttpJsonDatas(url, dataParams, cookies, Method.GET,null);
	}
	
	public static JSONObject getHttpPostJsonDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies) {
		return getHttpJsonDatas(url, dataParams, cookies, Method.POST,null);
	}
	
	
	public static JSONObject getHttpGetJsonDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Hashtable<String, String> headerParams) {
		return getHttpJsonDatas(url, dataParams, cookies, Method.GET,headerParams);
	}
	
	public static JSONObject getHttpPostJsonDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Hashtable<String, String> headerParams) {
		return getHttpJsonDatas(url, dataParams, cookies, Method.POST,headerParams);
	}
	
	
	public static JSONObject getHttpJsonDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Method method,Hashtable<String, String> headerParams) {
		JSONObject getJson = getHttpDatas(url, dataParams, cookies, method,headerParams);
		if(getJson.getBooleanValue("isok")) {
			try {
				JSONObject backJson = JSON.parseObject(getJson.getString("body"));
				getJson.put("bodyjson", backJson);
			}catch (Exception e) {
				System.out.println("url:"+url);
				for (String key : dataParams.keySet()) {
					System.out.println(key+":"+dataParams.get(key));
				}
				// TODO: handle exception
				e.printStackTrace();
				JSONObject backObj = new JSONObject();
				backObj.put("code", -99);
				backObj.put("errmsg", "访问链接出现异常："+e.getMessage());
				return backObj;
			}
		}
		return getJson;
	}
	
	
	public static JSONObject getHttpDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Method method) {
		// TODO Auto-generated method stub
		return getHttpDatas(url, dataParams, cookies, method, null);
	}
	
	
	public static JSONObject getHttpDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Method method, Hashtable<String, String> headerParams) {
		// TODO Auto-generated method stub
		JSONObject backObj = new JSONObject();
		Connection Con = Jsoup.connect(url);
		Con.header(USER_AGENT, USER_AGENT_VALUE);
		if(headerParams!=null) {
			for(String key : headerParams.keySet()) {
				Con.header(key, headerParams.get(key));
			}
		}
		Con.timeout(28000);
		try {
			Response ConResponse = Con.ignoreContentType(true).followRedirects(true).method(method).data(dataParams).cookies(cookies).execute();
			if(ConResponse.statusCode()==200) {
				/*if(url.trim().equals("https://ah.kj2100.com/web/portal/play/getPlayParams")) {
					System.out.println("请求URL"+url);
					System.out.println("返回内容："+ConResponse.body());
					System.out.println("==============header==========");
					for(String key : headerParams.keySet()) {
						System.out.println(key+":"+headerParams.get(key));
					}
					System.out.println("==============header==========");
					System.out.println("==============postData==========");
					for(String key : dataParams.keySet()) {
						System.out.println(key+":"+dataParams.get(key));
					}
					System.out.println("==============postData==========");
					System.out.println("==============cookies==========");
					System.out.println("cookies-old："+cookies);
					System.out.println("cookies-back："+ConResponse.cookies());
					System.out.println("==============postData==========");
				}*/
				backObj.put("isok", true);
				backObj.put("cookies", ConResponse.cookies());
				backObj.put("body",ConResponse.body());
			}else {
				backObj.put("isok", false);
				backObj.put("code", -99);
				backObj.put("errmsg", "访问链接出现异常：请求返回状态"+ConResponse.statusCode());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			backObj.put("isok", false);
			backObj.put("code", -99);
			backObj.put("errmsg", "访问链接出现异常："+e.getMessage());
		}
		return backObj;
	}


	public static JSONObject getLoginDatas(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Method method) {
		// TODO Auto-generated method stub
		Connection Con = Jsoup.connect(url);
		Con.header(USER_AGENT, USER_AGENT_VALUE);
		Con.timeout(28000);
		JSONObject results = new JSONObject();
		try {
			Con = Con.ignoreContentType(true).followRedirects(true).method(method).data(dataParams);
			if(cookies!=null) {
				Con = Con.cookies(cookies);
			}
			Response ConResponse = Con.execute();
			if(ConResponse.statusCode()==200) {
				System.out.println(ConResponse.cookies().size());
				results.put("backbody", ConResponse.body());
				results.put("cookies", ConResponse.cookies());
				results.put("isok", true);
			}else {
				results.put("isok", false);
				results.put("code", -99);
				results.put("errmsg", "访问链接出现异常：请求返回状态"+ConResponse.statusCode());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			results.put("isok", false);
			results.put("code", -99);
			results.put("errmsg", "访问链接出现异常："+e.getMessage());
		}
		return results;
	}
	
	public static JSONObject getValidateCode(String url, Hashtable<String, String> dataParams, Map<String, String> cookies,Method method) {
		// TODO Auto-generated method stub
		Connection Con = Jsoup.connect(url);
		Con.header(USER_AGENT, USER_AGENT_VALUE);
		Con.timeout(28000);
		JSONObject results = new JSONObject();
		try {
			Con = Con.ignoreContentType(true).followRedirects(true).method(method).data(dataParams);
			if(cookies!=null) {
				Con = Con.cookies(cookies);
			}
			Response ConResponse = Con.execute();
			if(ConResponse.statusCode()==200) {
		        //得到图片的二进制数据，以二进制封装得到数据，具有通用性  
		        byte[] data = ConResponse.bodyAsBytes();
		        //new一个文件对象用来保存图片，默认保存当前工程根目录  
				results.put("image", data);
				results.put("cookies", ConResponse.cookies());
				results.put("isok", true);
			}else {
				results.put("isok", false);
				results.put("code", -99);
				results.put("errmsg", "访问链接出现异常：请求返回状态"+ConResponse.statusCode());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			results.put("isok", false);
			results.put("code", -99);
			results.put("errmsg", "访问链接出现异常："+e.getMessage());
		}
		return results;
	}

	public static JSONObject getMachRegexJSON(String content, JSONObject machParmas) {
		// TODO Auto-generated method stub
		JSONObject result  = new JSONObject();
		for (String regexKey : machParmas.keySet()) {
			String regex = machParmas.getString(regexKey);
			System.out.println(regex);
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				String temp = matcher.group();
				result.put(regexKey, temp);
			}
		}
		return result;
	}
	
	
	public static void mains(String[] args) {
		String content = "\r\n" + 
				"ssoLogin={\r\n" + 
				"    lt:\"LT-6150299-xkIpujBBk6v2SEMqJhDG4vidhWl371-LT\",\r\n" + 
				"    execution:\"e1s1\",\r\n" + 
				"    eventId:\"submit\"\r\n" + 
				"};\r\n" + 
				"\r\n" + 
				"ssoLogin.login=function(loginParam,extAttribute){\r\n" + 
				"\r\n" + 
				"    return $.ajax({\r\n" + 
				"        url: 'https://hwssov1.59iedu.com/login;SSO_LT_JSESSIONID=1F9361008B796132C8F05EEA73F2B284',\r\n" + 
				"        type: 'post',\r\n" + 
				"        data: {\"lt\":ssoLogin.lt,\r\n" + 
				"            \"execution\":ssoLogin.execution,\r\n" + 
				"            \"_eventId\":ssoLogin.eventId,\r\n" + 
				"            \"userId\":loginParam.ssoUid,\r\n" + 
				"            \"extAttribute\":extAttribute\r\n" + 
				"        },\r\n" + 
				"        asnyc:false,\r\n" + 
				"        dataType: 'jsonp',\r\n" + 
				"        jsonp:'ssoCb',\r\n" + 
				"        success: function (data) {\r\n" + 
				"            if (data.code== 603){\r\n" + 
				"                if (typeof cauth_login_ticket_timer_id != \"undefined\"){\r\n" + 
				"                    window.clearInterval(cauth_login_ticket_timer_id);\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"            processLogin(data);\r\n" + 
				"        }\r\n" + 
				"    });\r\n" + 
				"};\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"if (typeof cauth_login_ticket_timer_id != \"undefined\"){\r\n" + 
				"    window.clearInterval(cauth_login_ticket_timer_id);\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"var cauth_login_ticket_timer_id= window.setInterval(function(){\r\n" + 
				"    var loginScript = $ ( '#_login_script' );\r\n" + 
				"    if ( loginScript.length > 0 ) {\r\n" + 
				"        loginScript.remove ();\r\n" + 
				"    }\r\n" + 
				"    var script  = document.createElement ( \"script\" );\r\n" + 
				"    script.id   = '_login_script';\r\n" + 
				"    script.type = \"text/javascript\";\r\n" + 
				"    script.src  = \"https://hwssov1.59iedu.com/login;SSO_LT_JSESSIONID=1F9361008B796132C8F05EEA73F2B284?_=1605257395644&userId=2c9080ef752ba11301753fceb3b66de1&nocache=\" + new Date ().getTime ();\r\n" + 
				"    document.getElementsByTagName ( 'head' )[0].appendChild ( script );\r\n" + 
				"},60000);\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"";
		JSONObject machParmas = new JSONObject();
		machParmas.put("lt", "^(LT\\-)(\\w+)(\\-LT)$");
		machParmas.put("execution", "^execution\\:\"\\w+\"$");
		machParmas.put("eventId", "eventId\\:\"\\w+\"$");
		content = content.replaceAll("\r\n", "");
		JSONObject parmas= JSON.parseObject(content.substring(content.indexOf("ssoLogin=")+9,content.indexOf("ssoLogin.login=")-1));
		System.out.println(parmas.getString("execution"));
		System.out.println(parmas.getString("lt"));
		System.out.println(parmas.getString("eventId"));
	}


	public static String getHttpGetBackBody(String url, Hashtable<String, String> dataParams,
			Map<String, String> cookies, Method method) {
		// TODO Auto-generated method stub
		String body = null;
		Connection Con = Jsoup.connect(url);
		Con.header(USER_AGENT, USER_AGENT_VALUE);
		Con.timeout(28000);
		try {
			Response ConResponse = Con.ignoreContentType(true).method(method).data(dataParams).cookies(cookies).execute();
			if(ConResponse.statusCode()==200) {
				cookies = ConResponse.cookies();
				System.out.println("cookies:"+cookies);
				body = ConResponse.body();
			}else {
				System.out.println("访问链接出现异常：请求返回状态:"+ConResponse.statusCode());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("访问链接出现异常："+e.getMessage());
		}
		return body;
	}
	
	
	
	
	public static String getHttpSendBody(String url, String sendBody,
			Map<String, String> cookies, Method method, Hashtable<String, String> headerParams) {
		// TODO Auto-generated method stub
		String body = null;
		Connection Con = Jsoup.connect(url);
		Con.header(USER_AGENT, USER_AGENT_VALUE);
		if(headerParams!=null) {
			for(String key : headerParams.keySet()) {
				Con.header(key, headerParams.get(key));
			}
		}
		Con.header("Content-Type", "application/json").timeout(28000).requestBody(sendBody).maxBodySize(2048);
		try {
			Response ConResponse = Con.ignoreContentType(true).method(method).cookies(cookies).execute();
			if(ConResponse.statusCode()==200) {
				cookies = ConResponse.cookies();
				body = ConResponse.body();
			}else {
				System.out.println("访问链接出现异常：请求返回状态:"+ConResponse.statusCode());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("访问链接出现异常："+e.getMessage());
		}
		return body;
	}


	public static JSONObject requstHttpOlny(String url, Hashtable<String, String> dataParams,Map<String, String> cookies, Method method) {
		// TODO Auto-generated method stub
		JSONObject result = new  JSONObject();
		Connection Con = Jsoup.connect(url);
		Con.header(USER_AGENT, USER_AGENT_VALUE);
		Con.timeout(28000);
		try {
			Response ConResponse = Con.ignoreContentType(true).method(method).data(dataParams).cookies(cookies).execute();
			result.put("statusCode",ConResponse.statusCode());
			if(ConResponse.statusCode()==200) {
				result.put("body",ConResponse.body());
				result.put("isok",true);
			}else {
				result.put("isok",false);
				result.put("errmsg","访问链接出现异常：请求返回状态:"+ConResponse.statusCode());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.put("isok",false);
			result.put("errmsg","访问链接出现异常："+e.getMessage());
		}
		return result;
	}


	public static JSONObject getHttpGetJsonData(String url, Hashtable<String, String> dataParams,
			Map<String, String> cookies, Hashtable<String, String> headerParams) {
		// TODO Auto-generated method stub
		return getHttpGetJsonDatas(url, dataParams, cookies, headerParams).getJSONObject("bodyjson");
	}
	
	
	public static void main(String[] args) {
		Map<String, String> cookies = new HashMap<String, String>();
		Hashtable<String, String> headerParams = new Hashtable<String, String>();
		String sendBody = "{\"air_data\": [{\"powerSate\": \"POWER_ON\", \"voltage\": \"237\", \"electric_Current\": \"3.343\", \"activePower\": \"1069.5\", \"apparentPower\": \"794.9\", \"powerFactor\": \"1.00\", \"electricity\": \"1.354\", \"temperature\": \"28.5 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"238\", \"electric_Current\": \"4.237\", \"activePower\": \"1055.6\", \"apparentPower\": \"1009.6\", \"powerFactor\": \"1.00\", \"electricity\": \"1.356\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"238\", \"electric_Current\": \"4.237\", \"activePower\": \"1055.6\", \"apparentPower\": \"1009.6\", \"powerFactor\": \"1.00\", \"electricity\": \"1.356\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"238\", \"electric_Current\": \"4.237\", \"activePower\": \"1055.6\", \"apparentPower\": \"1009.6\", \"powerFactor\": \"1.00\", \"electricity\": \"1.356\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"237\", \"electric_Current\": \"5.230\", \"activePower\": \"1257.7\", \"apparentPower\": \"1242.8\", \"powerFactor\": \"1.00\", \"electricity\": \"1.357\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"237\", \"electric_Current\": \"5.230\", \"activePower\": \"1257.7\", \"apparentPower\": \"1242.8\", \"powerFactor\": \"1.00\", \"electricity\": \"1.357\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"234\", \"electric_Current\": \"9.567\", \"activePower\": \"2820.1\", \"apparentPower\": \"2244.7\", \"powerFactor\": \"1.00\", \"electricity\": \"1.359\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"234\", \"electric_Current\": \"9.567\", \"activePower\": \"2820.1\", \"apparentPower\": \"2244.7\", \"powerFactor\": \"1.00\", \"electricity\": \"1.359\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"234\", \"electric_Current\": \"9.567\", \"activePower\": \"2820.1\", \"apparentPower\": \"2244.7\", \"powerFactor\": \"1.00\", \"electricity\": \"1.359\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"233\", \"electric_Current\": \"12.879\", \"activePower\": \"2888.9\", \"apparentPower\": \"3007.3\", \"powerFactor\": \"0.96\", \"electricity\": \"1.361\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"233\", \"electric_Current\": \"12.879\", \"activePower\": \"2888.9\", \"apparentPower\": \"3007.3\", \"powerFactor\": \"0.96\", \"electricity\": \"1.361\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"233\", \"electric_Current\": \"12.879\", \"activePower\": \"2888.9\", \"apparentPower\": \"3007.3\", \"powerFactor\": \"0.96\", \"electricity\": \"1.361\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"234\", \"electric_Current\": \"10.856\", \"activePower\": \"2439.6\", \"apparentPower\": \"2549.8\", \"powerFactor\": \"0.95\", \"electricity\": \"1.363\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"234\", \"electric_Current\": \"10.856\", \"activePower\": \"2439.6\", \"apparentPower\": \"2549.8\", \"powerFactor\": \"0.95\", \"electricity\": \"1.363\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"234\", \"electric_Current\": \"10.856\", \"activePower\": \"2439.6\", \"apparentPower\": \"2549.8\", \"powerFactor\": \"0.95\", \"electricity\": \"1.363\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"10.125\", \"activePower\": \"2328.7\", \"apparentPower\": \"2379.3\", \"powerFactor\": \"0.97\", \"electricity\": \"1.365\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"10.125\", \"activePower\": \"2328.7\", \"apparentPower\": \"2379.3\", \"powerFactor\": \"0.97\", \"electricity\": \"1.365\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"10.125\", \"activePower\": \"2328.7\", \"apparentPower\": \"2379.3\", \"powerFactor\": \"0.97\", \"electricity\": \"1.365\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.874\", \"activePower\": \"2275.2\", \"apparentPower\": \"2320.4\", \"powerFactor\": \"0.98\", \"electricity\": \"1.367\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.874\", \"activePower\": \"2275.2\", \"apparentPower\": \"2320.4\", \"powerFactor\": \"0.98\", \"electricity\": \"1.367\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.874\", \"activePower\": \"2275.2\", \"apparentPower\": \"2320.4\", \"powerFactor\": \"0.98\", \"electricity\": \"1.367\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.696\", \"activePower\": \"2236.6\", \"apparentPower\": \"2278.7\", \"powerFactor\": \"0.98\", \"electricity\": \"1.369\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.696\", \"activePower\": \"2236.6\", \"apparentPower\": \"2278.7\", \"powerFactor\": \"0.98\", \"electricity\": \"1.369\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.696\", \"activePower\": \"2236.6\", \"apparentPower\": \"2278.7\", \"powerFactor\": \"0.98\", \"electricity\": \"1.369\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.560\", \"activePower\": \"2201.3\", \"apparentPower\": \"2246.8\", \"powerFactor\": \"0.97\", \"electricity\": \"1.371\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.560\", \"activePower\": \"2201.3\", \"apparentPower\": \"2246.8\", \"powerFactor\": \"0.97\", \"electricity\": \"1.371\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.135\", \"activePower\": \"1935.9\", \"apparentPower\": \"2147.8\", \"powerFactor\": \"0.90\", \"electricity\": \"1.372\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.135\", \"activePower\": \"1935.9\", \"apparentPower\": \"2147.8\", \"powerFactor\": \"0.90\", \"electricity\": \"1.372\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"235\", \"electric_Current\": \"9.135\", \"activePower\": \"1935.9\", \"apparentPower\": \"2147.8\", \"powerFactor\": \"0.90\", \"electricity\": \"1.372\", \"temperature\": \"28.8 \"}, {\"powerSate\": \"POWER_ON\", \"voltage\": \"236\", \"electric_Current\": \"7.127\", \"activePower\": \"1528.2\", \"apparentPower\": \"1685.5\", \"powerFactor\": \"0.90\", \"electricity\": \"1.373\", \"temperature\": \"28.8 \"}]}";
		System.out.println(getHttpSendBody("http://zsg.uiframe.com/api/air_conditioning_detection/", sendBody, cookies, Method.POST, headerParams));
	}
	
}
