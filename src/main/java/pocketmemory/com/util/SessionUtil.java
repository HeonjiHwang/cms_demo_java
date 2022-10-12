package pocketmemory.com.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pocketmemory.com.util.EncryptUtil.RSAKey;

public class SessionUtil implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);
	
	private static SessionUtil instance = new SessionUtil();
	
	private static HashMap<String, Object> sessionMap = new HashMap<String, Object>();
	
	private SessionUtil() {}
	
	public static SessionUtil getInstance() {
		return instance;
	}
	
	public class LoginInfo{
		public Integer user_cd;
		public String user_id;
		private String user_name;
		public Integer authority_id;
		public String authority_name;
		public String email;
	}
	
	public void clearSession(HttpSession session) {
		if(session == null) return;
		
		session.setAttribute("isLogin", false);
		session.setAttribute("user_cd", null);
		session.setAttribute("user_id", "");
		session.setAttribute("user_name", "");
		session.setAttribute("authority_id", null);
		session.setAttribute("authority_name", "");
		session.setAttribute("email", "");
	}
	
	public void addKeySession(HttpSession session) {
		try {
			RSAKey rsaKey = EncryptUtil.getInstance().genRSAKeyPair();
			
			session.setAttribute("public_key", rsaKey._publicKey);
			session.setAttribute("private_key", rsaKey._privateKey);
			session.setAttribute("modulus", rsaKey.modulus);
			session.setAttribute("exponent", rsaKey.exponent);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addSession(HttpSession session, List<CaseInsensitiveMap> listData) {
		if(session == null) return;
		
		session.setAttribute("isLogin", true);
		session.setAttribute("user_cd", listData.get(0).get("user_cd"));
		session.setAttribute("user_id", XssWebUtil.clearXSSMaximum(listData.get(0).get("user_id").toString()));
		session.setAttribute("user_name", XssWebUtil.clearXSSMaximum(listData.get(0).get("user_name").toString()));
		session.setAttribute("authority_id", listData.get(0).get("authority_id"));
		session.setAttribute("authority_name", XssWebUtil.clearXSSMaximum(listData.get(0).get("authority_name").toString()));
		session.setAttribute("email", listData.get(0).get("email") != null ? XssWebUtil.clearXSSMaximum(listData.get(0).get("email").toString()) : null);
	}
	
	public void addRedisSession(HttpSession session, StringRedisTemplate redisTemplate) {
		if(session == null) return;
		
		LoginInfo loginInfo = getLoginInfo(session, redisTemplate);
		
		if(loginInfo != null) {
			String key = "USER:" + loginInfo.user_id;
			
			try {
				Boolean sessionChk = redisTemplate.hasKey(key);
				
				if(!sessionChk) {
					JsonObject obj = new JsonObject();
					obj.addProperty("user_id", loginInfo.user_id);
					obj.addProperty("session_id", session.getId());
					
					SimpleDateFormat datetime = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
					obj.addProperty("datetime", datetime.format(new Date(System.currentTimeMillis())));
					
					redisTemplate.opsForValue().set(key, obj.toString(), 30, TimeUnit.MINUTES);
				}else {
					String redisSession = redisTemplate.opsForValue().get(key);
					
					JsonObject sessionObj = JsonParser.parseString(redisSession).getAsJsonObject();
					
					if(!session.getId().equals(sessionObj.get("session_id"))) {
						redisTemplate.delete("spring:session:sessions:"+sessionObj.get("session_id"));
						redisTemplate.delete("spring:session:sessions:expires:" + sessionObj.get("session_id"));
						
						JsonObject obj = new JsonObject();
						obj.addProperty("user_id", loginInfo.user_id);
						obj.addProperty("session_id", session.getId());
						
						SimpleDateFormat datetime = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
						obj.addProperty("datetime", datetime.format(new Date(System.currentTimeMillis())));
						
						redisTemplate.opsForValue().set(key, obj.toString(), 30, TimeUnit.MINUTES);
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}else {
			
		}
	}
	
	public void removeRedisSession(HttpSession session, StringRedisTemplate redisTemplate) {
		if(session == null) return;
		
		LoginInfo loginInfo = getLoginInfo(session, redisTemplate);
		
		if(loginInfo != null) {
			if(loginInfo.user_id != null) {
				String key = "USER:" + loginInfo.user_id;
				
				try {
					redisTemplate.delete(key);
					redisTemplate.delete("spring:session:sessions:"+session.getId());
					redisTemplate.delete("spring:session:sessions:expires:" + session.getId());
				}catch(Exception e) {}
			}
			
			clearSession(session);
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public Boolean checkRedisSession(String user_id, HttpSession session, StringRedisTemplate redisTemplate) {
		String key = "USER:" + user_id;
		
		try {
			String redisSession = redisTemplate.opsForValue().get(key);
			
			if(redisSession == null) return false;
			
			JsonObject sessionObj = JsonParser.parseString(redisSession).getAsJsonObject();
			System.out.println(sessionObj.get("session_id"));
			
			if(sessionObj.get("session_id") == null)
				return false;
			
			if(session.getId().equals(sessionObj.get("session_id"))) return true;
			
		}catch(Exception e) {}
		
		return false;
	}
	
	public List<String> getAllRedisKeys(StringRedisTemplate redisTemplate){
		Set<String> redisKeys = redisTemplate.keys("USER:*");
		List<String> keyLists = new ArrayList<>();
		Iterator<String> it = redisKeys.iterator();
		while(it.hasNext()) {
			String data = it.next();
			String user_id = data.split(":")[1];
			keyLists.add(user_id);
		}
		
		return keyLists;
	}
	
	public LoginInfo getLoginInfo(HttpSession session, StringRedisTemplate redisTemplate) {
		if(session == null) return null;
		
		try {
			Boolean isLogin = (Boolean) session.getAttribute("isLogin");
			
			if(isLogin == null || isLogin.booleanValue() == false) return null;
			if(session.getAttribute("user_id").toString() == null || session.getAttribute("user_id").toString().equals("")) return null;
			if(session.getAttribute("authority_id") == null || (Integer) session.getAttribute("authority_id") < 0) return null;
			
			LoginInfo loginInfo = new LoginInfo();
			
			loginInfo.user_cd = (Integer) session.getAttribute("user_cd");
			loginInfo.user_id = (String) session.getAttribute("user_id");
			loginInfo.user_name = (String) session.getAttribute("user_name");
			loginInfo.email = (String) session.getAttribute("email");
			loginInfo.authority_id = (Integer) session.getAttribute("authority_id");
			loginInfo.authority_name = (String) session.getAttribute("authority_name");
			
			// Update user status
			if (redisTemplate != null) {
				try {
					String key = "USER:" + loginInfo.user_id;
					
					JsonObject jObj = new JsonObject();
					jObj.addProperty("user_id", loginInfo.user_id);
					jObj.addProperty("session_id", session.getId());
					jObj.addProperty("last_time", Long.toString(System.currentTimeMillis()/1000));
					
					SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					jObj.addProperty("last_str_time", dayTime.format(new Date(System.currentTimeMillis())));
                    
					redisTemplate.opsForValue().set(key, jObj.toString(), 30, TimeUnit.MINUTES);
				} catch(Exception e) {}
			}
			
			return loginInfo;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
