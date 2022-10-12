package pocketmemory.com.data.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.CaseInsensitiveMap;

public interface UserManagementMapper {
	
	List<CaseInsensitiveMap> selectUsersInfo(HashMap<String, Object> params);
	
	int updateUserInfo(HashMap<String, Object> params);
	
	int deleteUserInfo(HashMap<String, Object> params);
	
	List<CaseInsensitiveMap> selectAuthorityInfo();
	
	List<CaseInsensitiveMap> checkIDValidation(HashMap<String, Object> params);
	
	List<CaseInsensitiveMap> selectReqUserInfo();
	
	int insertReqUserInfo(HashMap<String, Object> params);
	
	int insertReqUsertoUserInfo(HashMap<String, Object> params);
	
	int deleteReqUserInfo(HashMap<String, Object> params);
	
	List<CaseInsensitiveMap> selectUserAccessInfo();
}
