package pocketmemory.com.data.dao;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import pocketmemory.com.data.entity.UserAccessInfo;

@Repository("IndexDAO")
public interface IndexDAO {
	
	public List<CaseInsensitiveMap> selectUserInfo(HashMap<String, Object> params);
	
	public int insertUserInfo(HashMap<String, Object> params);
	
	public List<CaseInsensitiveMap> selectMenuInfo(HashMap<String, Object> params);
	
	public int insertUserAccessInfo(UserAccessInfo userAccessInfo, HttpSession session, StringRedisTemplate redisTemplate);
	
	public int updateLastLoginInfo(HashMap<String, Object> params);
}
