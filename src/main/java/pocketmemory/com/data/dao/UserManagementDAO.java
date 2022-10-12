package pocketmemory.com.data.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.springframework.stereotype.Repository;

@Repository("UserManagementDAO")
public interface UserManagementDAO {

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
