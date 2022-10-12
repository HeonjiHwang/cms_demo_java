package pocketmemory.com.controller;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
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
import pocketmemory.com.data.dao.UserManagementDAOImpl;
import pocketmemory.com.data.entity.UserAccessInfo;
import pocketmemory.com.util.EncryptUtil;
import pocketmemory.com.util.JsonResult;
import pocketmemory.com.util.ReadDataUtil;
import pocketmemory.com.util.SessionUtil;
import pocketmemory.com.util.SessionUtil.LoginInfo;
import pocketmemory.com.util.XssWebUtil;

@Controller
@RequestMapping("/data")
public class UserManagementDataController {
	private static final Logger logger = LoggerFactory.getLogger(IndexDataController.class);
	private static final Gson gson = new Gson();
	
	@Inject
	@Autowired
	private UserManagementDAOImpl userManagementDAOImpl;
	
	@Inject
	@Autowired
	private IndexDAOImpl indexDAOImpl;
	 
	@Autowired
	StringRedisTemplate redisTemplate;
	
	ReadDataUtil dataUtil = new ReadDataUtil();
	
	@RequestMapping(value = "/getUsersInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Object getUsersInfo(Model model, HttpSession session, HttpServletRequest request)
    {
        logger.info("UserManagementDataController.java getUsersInfo.");
        
        JsonResult result = new JsonResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
                
        try {
        	LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
        	
        	if(loginInfo == null) {
        		result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
        	}
        	
        	params.put("authority_id", loginInfo.authority_id);
        	
        	List<CaseInsensitiveMap> listData = userManagementDAOImpl.selectUsersInfo(params);
        	List<String> onlineList = SessionUtil.getInstance().getAllRedisKeys(redisTemplate);
        	
        	for(int i=0;i<listData.size();i++) {
        		String user_id = (String) listData.get(i).get("user_id");
        		
        		if(onlineList.contains(user_id)) {
        			listData.get(i).put("is_online", 1);
        		}else {
        			listData.get(i).put("is_online", 0);
        		}
        	}
        	
        	if(listData.size() > 0) {
        		result.setData(listData);
        		result.setSuccess(true);
        	}else {
        		result.setSuccess(false);
        	}
        	
        	/*사용자 접속 정보*/
        	UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserManagement");
			userAccessInfo.setAction_type("S");
			userAccessInfo.setIs_success(listData.size() > 0 ? 1 : 2);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return gson.toJson(result);
    }

	@RequestMapping(value="/updateUserInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object updateUserInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("UserManagementDataConroller.java updateUserInfo {}.", locale);
		
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
			
			/* 복호화 */
			if(obj.get("user_pwd") != null) {
				PrivateKey key = (PrivateKey) session.getAttribute("private_key");
				String _s_password = obj.get("user_pwd").getAsString();
				String _d_password = _s_password != null ? EncryptUtil.getInstance().decryptRSA(key, _s_password) : null;
				params.put("user_pwd", _d_password);
			}
			
			params.put("user_id", obj.get("user_id").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("user_id").getAsString()) : null);
			params.put("user_name", obj.get("user_name").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("user_name").getAsString()) : null);
			params.put("email", obj.get("email").getAsString() != null ? XssWebUtil.clearXSSMinimum(obj.get("email").getAsString()) : null);
			params.put("is_enable", obj.get("is_enable").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMinimum(obj.get("is_enable").getAsString())) : null);
			params.put("authority_id", obj.get("authority_id").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMinimum(obj.get("authority_id").getAsString())) : null);
			params.put("user_cd", obj.get("user_cd").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMinimum(obj.get("user_cd").getAsString())) : null);
			
			int iResult = userManagementDAOImpl.updateUserInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserManagement");
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

	@RequestMapping(value="/deleteUserInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object deleteUserInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("UserManagementDataConroller.java deleteUserInfo {}.", locale);
		
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
			
			params.put("user_cd", obj.get("user_cd").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMaximum(obj.get("user_cd").getAsString())) : null);
			
			int iResult = userManagementDAOImpl.deleteUserInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserManagement");
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
	
	@RequestMapping(value = "/getAuthorityInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Object getAuthorityInfo(Model model, HttpSession session, HttpServletRequest request)
    {
        logger.info("UserManagementDataController.java getAuthorityInfo.");
        
        JsonResult result = new JsonResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
                
        try {
        	LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
        	
        	if(loginInfo == null) {
        		result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
        	}
        	
        	List<CaseInsensitiveMap> listData = userManagementDAOImpl.selectAuthorityInfo();
        	
        	if(listData.size() > 0) {
        		result.setData(listData);
        		result.setSuccess(true);
        	}else {
        		result.setSuccess(false);
        	}
        	
        	/*사용자 접속 정보*/
        	UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserManagement");
			userAccessInfo.setAction_type("S");
			userAccessInfo.setIs_success(listData.size() > 0 ? 1 : 0);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return gson.toJson(result);
    }
	
	@RequestMapping(value = "/getReqUserInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Object getReqUserInfo(Model model, HttpSession session, HttpServletRequest request)
    {
        logger.info("UserManagementDataController.java getReqUserInfo.");
        
        JsonResult result = new JsonResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
                
        try {
        	LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
        	
        	if(loginInfo == null) {
        		result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
        	}
        	
        	List<CaseInsensitiveMap> listData = userManagementDAOImpl.selectReqUserInfo();
        	
    		result.setData(listData);
    		result.setSuccess(true);
        	
        	/*사용자 접속 정보*/
        	UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserRequestInfo");
			userAccessInfo.setAction_type("S");
			userAccessInfo.setIs_success(1);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return gson.toJson(result);
    }
	
	@RequestMapping(value="/setReqUserInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object setReqUserInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("UserManagementDataConroller.java setReqUserInfo {}.", locale);
		
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		
		try {		
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			//check id validation first
			params.put("user_id", obj.get("user_id").getAsString() != null ? XssWebUtil.clearXSSMaximum(obj.get("user_id").getAsString()) : null);
			List<CaseInsensitiveMap> listData = userManagementDAOImpl.checkIDValidation(params);
			
			if(listData.size() > 0) {
				result.setSuccess(false);
				result.setErrorMessage("id is already exists");
				return gson.toJson(result);
			}
			
			PrivateKey key = (PrivateKey) session.getAttribute("private_key");
			params.clear();
			
			String _s_password = obj.get("user_pwd").getAsString();
			String _d_password = _s_password != null ? EncryptUtil.getInstance().decryptRSA(key, _s_password) : null;
			
			params.put("user_id", obj.get("user_id").getAsString() != null ? XssWebUtil.clearXSSMaximum(obj.get("user_id").getAsString()) : null);
			params.put("user_name", obj.get("user_name").getAsString() != null ? XssWebUtil.clearXSSMaximum(obj.get("user_name").getAsString()) : null);
			params.put("email", obj.get("email").getAsString() != null ? XssWebUtil.clearXSSMaximum(obj.get("email").getAsString()) : null);
			params.put("user_pwd", _d_password);
			
			int iResult = userManagementDAOImpl.insertReqUserInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserRequestInfo");
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
	
	@RequestMapping(value="/setReqUsertoUserInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object setReqUsertoUserInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("UserManagementDataConroller.java setReqUsertoUserInfo {}.", locale);

		HttpSession session = request.getSession(true);
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		try {
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			
			if(loginInfo == null) {
				result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
			}
			
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			params.put("user_req_cd", obj.get("user_req_cd").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMaximum(obj.get("user_req_cd").getAsString())) : null);
			params.put("authority_id", obj.get("authority_id").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMaximum(obj.get("authority_id").getAsString())) : null);
			params.put("is_enable", obj.get("is_enable").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMaximum(obj.get("is_enable").getAsString())) : null);
			
			int iResult = userManagementDAOImpl.insertReqUsertoUserInfo(params);
			
			if(iResult > 0) {
				int dResult = userManagementDAOImpl.deleteReqUserInfo(params);
				
				if(dResult > 0) {
					result.setSuccess(true);
				}else {
					result.setSuccess(false);
				}
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserRequestInfo");
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
	
	@RequestMapping(value="/deleteReqUserInfo", produces="application/json;charset=UTF-8", method = RequestMethod.POST)
	@ResponseBody
	public Object deleteReqUserInfo(Locale locale, Model model, HttpServletRequest request) {
		logger.info("UserManagementDataConroller.java setReqUsertoUserInfo {}.", locale);

		HttpSession session = request.getSession(true);
		JsonResult result = new JsonResult();
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		try {
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			
			if(loginInfo == null) {
				result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
			}
			
			JsonObject obj = dataUtil.getBodyData( request.getReader());
			
			params.put("user_req_cd", obj.get("user_req_cd").getAsString() != null ? Integer.parseInt(XssWebUtil.clearXSSMaximum(obj.get("user_req_cd").getAsString())) : null);
			
			int iResult = userManagementDAOImpl.deleteReqUserInfo(params);
			
			if(iResult > 0) {
				result.setSuccess(true);
			}else {
				result.setSuccess(false);
			}

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserRequestInfo");
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
	
	@RequestMapping(value = "/getUserAccessInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Object getUserAccessInfo(Model model, HttpSession session, HttpServletRequest request)
    {
        logger.info("UserManagementDataController.java getUserAccessInfo.");
        
        JsonResult result = new JsonResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
                
        try {
        	LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
        	
        	if(loginInfo == null) {
        		result.setSuccess(false);
				result.setErrorMessage("session closed");
				return gson.toJson(result);
        	}
        	
        	List<CaseInsensitiveMap> listData = userManagementDAOImpl.selectUserAccessInfo();
        	
    		result.setData(listData);
    		result.setSuccess(true);

			//사용자 접속 정보 저장 DB INSERT
			UserAccessInfo userAccessInfo = new UserAccessInfo();
			userAccessInfo.setMenu("UserAccessList");
			userAccessInfo.setAction_type("S");
			userAccessInfo.setIs_success(1);
			userAccessInfo.setAccess_ip(dataUtil.getIpAddress(request));
			indexDAOImpl.insertUserAccessInfo(userAccessInfo, session, redisTemplate);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return gson.toJson(result);
    }
}
