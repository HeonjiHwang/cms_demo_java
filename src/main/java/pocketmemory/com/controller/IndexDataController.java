package pocketmemory.com.controller;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pocketmemory.com.data.dao.IndexDAOImpl;
import pocketmemory.com.data.entity.UserAccessInfo;
import pocketmemory.com.util.EncryptUtil;
import pocketmemory.com.util.JsonResult;
import pocketmemory.com.util.ReadDataUtil;
import pocketmemory.com.util.SessionUtil;
import pocketmemory.com.util.SessionUtil.LoginInfo;
import pocketmemory.com.util.XssWebUtil;

@Controller
@RequestMapping("data")
public class IndexDataController {
	private static final Logger logger = LoggerFactory.getLogger(IndexDataController.class);
	private static final Gson gson = new Gson();
	
	@Inject
	@Autowired
	private IndexDAOImpl indexDAOImpl;
	
	@Autowired
	StringRedisTemplate redisTemplate;
	
	ReadDataUtil dataUtil = new ReadDataUtil();
	
	@RequestMapping(value = "/getPublicKey", produces = "application/json;charset=UTF-8", method = RequestMethod.GET) // Select
    @ResponseBody
    public Object getPublicKey(Model model, HttpSession session, HttpServletRequest request)
    {
        logger.info("IndexDataController.java getPublicKey.");
        
        JsonResult result = new JsonResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
                
        try {        	
        	if(session.getAttribute("public_key") == null || session.getAttribute("private_key") == null) {	
        		SessionUtil.getInstance().addKeySession(session);
        	}
        	
        	PublicKey publicKey = (PublicKey) session.getAttribute("public_key");
        	String rsaModulus = (String) session.getAttribute("modulus");
        	String rsaExponent = (String) session.getAttribute("exponent");
        	
        	if(publicKey != null) {
        		params.put("publicKey", EncryptUtil.getInstance().publicKeyToString(publicKey));
        		params.put("rsaModulus", rsaModulus);
        		params.put("rsaExponent", rsaExponent);
        		
        		result.setData(params);
        		result.setSuccess(true);
        	}else {
        		result.setSuccess(false);
        	}
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return gson.toJson(result);
    }
	
	@RequestMapping(value="/signIn", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object signIn(Locale locale, Model model, HttpServletRequest request) {
		logger.info("IndexDataController.java signIn {}.", locale);
		
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		
		try {
			PrivateKey key = (PrivateKey) session.getAttribute("private_key");
			
			if(key == null) {
				result.setSuccess(false);
				result.setErrorMessage("key is not available. Refresh and try again");
				return gson.toJson(result);
			}
			
			Cookie[] cookies = request.getCookies();
			logger.info("================================== Cookie =================================");
			if(cookies != null && cookies.length > 0) {
				for(int i=0;i<cookies.length;i++) {
					logger.info("{}", cookies[i].getName());
					logger.info("{}", cookies[i].getValue());
				}
			}
			
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			String _s_password = obj.get("password").getAsString();
			String pwd = _s_password != null ? EncryptUtil.getInstance().decryptRSA(key, _s_password) : null;
			
			params.put("user_id", obj.get("user_id").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("user_id").getAsString()) : null);
			params.put("user_pwd", pwd);
			
			List<CaseInsensitiveMap> listData = indexDAOImpl.selectUserInfo(params);
			Integer isSuccess = 0;
			
			if(listData.size() > 0) {
				String isValid = listData.get(0).get("is_valid").toString();
				String isEnable = listData.get(0).get("is_enable").toString();
				
				if(isValid.compareToIgnoreCase("1") == 0 && isEnable.compareToIgnoreCase("1") == 0) {
					SessionUtil.getInstance().addSession(session, listData);
					SessionUtil.getInstance().addRedisSession(session, redisTemplate);
					
					isSuccess = 1;
					result.setSuccess(true);
					result.setData(listData);
					
					LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
					
					HashMap<String, Object> data = new HashMap<String, Object>();
					data.put("last_login_ip", dataUtil.getIpAddress(request));
					data.put("user_cd", loginInfo.user_cd);
					int isUpdate = indexDAOImpl.updateLastLoginInfo(data);
				}else {
					//로그인 실패!! > Password 오류
					SessionUtil.getInstance().clearSession(session);
					SessionUtil.getInstance().removeRedisSession(session, null);
					
					String resultMessage = null;
					if(isValid.compareToIgnoreCase("1") != 0)
						resultMessage = "Check your password";
					else if(isEnable.compareToIgnoreCase("1") != 0)
						resultMessage = "Your Account is not valid";
					
					result.setSuccess(false);
					result.setErrorMessage(resultMessage);
					result.setData(listData);
				}
			}else {
				//로그인 실패!! > ID 오류
				SessionUtil.getInstance().clearSession(session);
				SessionUtil.getInstance().removeRedisSession(session, null);
				
				result.setSuccess(false);
				result.setErrorMessage("Check your ID");
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("signin");
			userAccessInfo.setAction_type("S");
			userAccessInfo.setIs_success(isSuccess);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		}
		return gson.toJson(result);
	}
	
	@RequestMapping(value = "/signout", produces = "application/json;charset=UTF-8", method = RequestMethod.POST) // Select
    @ResponseBody
    public Object signOut(Locale locale, Model model, HttpServletRequest request) {
		logger.info("IndexDataController.java signOut {}.", locale);
		
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HttpSession session = request.getSession();
		
		try {
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			
			if(loginInfo == null) {
				result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setUser_cd(loginInfo.user_cd);
			userAccessInfo.setMenu("signout");
			userAccessInfo.setAction_type("");
			userAccessInfo.setIs_success(1);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
			
			SessionUtil.getInstance().removeRedisSession(session, redisTemplate);
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		}
		return gson.toJson(result);
	}
	
	@RequestMapping(value = "/getMenuAuthorityInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.GET) // Select
    @ResponseBody
    public Object getMenuAuthorityInfo(Model model, HttpSession session, HttpServletRequest request)
    {
        logger.info("IndexDataController.java getMenuAuthorityInfo.");
        
        JsonResult result = new JsonResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
                
        try {        	
        	LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);

        	if(loginInfo == null) {
        		result.setSuccess(false);
        		result.setErrorMessage("session closed");
        		return gson.toJson(result);
        	}
        	
        	params.put("authority_id", request.getParameter("authority_id") != null ? XssWebUtil.clearXSSMinimum(request.getParameter("authority_id")) : null);
        	params.put("is_all", request.getParameter("is_all") != null ? XssWebUtil.clearXSSMinimum(request.getParameter("is_all")) : null);
        	
        	List<CaseInsensitiveMap> listData = indexDAOImpl.selectMenuInfo(params);
        	
    		result.setSuccess(true);
    		result.setData(listData);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return gson.toJson(result);
    }
}
