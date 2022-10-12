package pocketmemory.com.data.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.CaseInsensitiveMap;

import pocketmemory.com.data.entity.UserAccessInfo;

public interface IndexMapper {
	
	public List<CaseInsensitiveMap> selectUserInfo(HashMap<String, Object> params);
	
	public int insertUserInfo(HashMap<String, Object> params);
	
	public List<CaseInsensitiveMap> selectMenuInfo(HashMap<String, Object> params);
	
	public int insertUserAccessInfo(UserAccessInfo userAccessInfo);
	
	public int updateLastLoginInfo(HashMap<String, Object> params);
}
