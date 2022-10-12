package pocketmemory.com.controller;

import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

import pocketmemory.com.data.dao.AuthorityManagementDAOImpl;
import pocketmemory.com.data.dao.IndexDAOImpl;
import pocketmemory.com.data.entity.UserAccessInfo;
import pocketmemory.com.util.JsonResult;
import pocketmemory.com.util.ReadDataUtil;
import pocketmemory.com.util.SessionUtil;
import pocketmemory.com.util.SessionUtil.LoginInfo;
import pocketmemory.com.util.XssWebUtil;

@Controller
@RequestMapping("data")
public class AuthorityManagementDataController {
	private static final Logger logger = LoggerFactory.getLogger(IndexDataController.class);
	private static final Gson gson = new Gson();
	
	@Inject
	@Autowired
	private IndexDAOImpl indexDAOImpl;
	
	@Inject
	@Autowired
	private AuthorityManagementDAOImpl authorityManagementDAOImpl;
	
	@Autowired
	StringRedisTemplate redisTemplate;
	
	ReadDataUtil dataUtil = new ReadDataUtil();
	
	@RequestMapping(value="/setAuthorityInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object setAuthorityInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("AuthorityManagementDataController.java setAuthorityInfo {}.", locale);
		
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		
		try {		
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			if(loginInfo == null) {
				result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
			}
			
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			params.put("authority_name", obj.get("authority_name").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("authority_name").getAsString()) : null);
			params.put("authority_desc", obj.get("authority_desc").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("authority_desc").getAsString()) : null);
			
			int iResult = authorityManagementDAOImpl.insertAuthorityInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserAuthorityManagement");
			userAccessInfo.setAction_type("I");
			userAccessInfo.setIs_success(iResult > 0 ? 1 : 0);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		}
		return gson.toJson(result);
	}
	
	@RequestMapping(value="/updateAuthorityInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object updateAuthorityInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("AuthorityManagementDataController.java updateAuthorityInfo {}.", locale);
		
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		
		try {		
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			if(loginInfo == null) {
				result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
			}
			
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			params.put("authority_id", obj.get("authority_id").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMinimum(obj.get("authority_id").getAsString())) : null);
			params.put("authority_name", obj.get("authority_name").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("authority_name").getAsString()) : null);
			params.put("authority_desc", obj.get("authority_desc").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("authority_desc").getAsString()) : null);
			
			int iResult = authorityManagementDAOImpl.updateAuthorityInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserAuthorityManagement");
			userAccessInfo.setAction_type("U");
			userAccessInfo.setIs_success(iResult > 0 ? 1 : 0);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		}
		return gson.toJson(result);
	}
	
	@RequestMapping(value="/deleteAuthorityInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object deleteAuthorityInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("AuthorityManagementDataController.java deleteAuthorityInfo {}.", locale);
		
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		
		try {		
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			if(loginInfo == null) {
				result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
			}
			
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			params.put("authority_id", obj.get("authority_id").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMinimum(obj.get("authority_id").getAsString())) : null);
			
			int iResult = authorityManagementDAOImpl.deleteAuthorityInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserAuthorityManagement");
			userAccessInfo.setAction_type("D");
			userAccessInfo.setIs_success(iResult > 0 ? 1 : 0);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		}
		return gson.toJson(result);
	}
	
}
