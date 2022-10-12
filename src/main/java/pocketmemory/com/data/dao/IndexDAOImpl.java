package pocketmemory.com.data.dao;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import pocketmemory.com.data.entity.UserAccessInfo;
import pocketmemory.com.data.mapper.IndexMapper;
import pocketmemory.com.util.SessionUtil;
import pocketmemory.com.util.SessionUtil.LoginInfo;

@Service("IndexService")
public class IndexDAOImpl implements IndexDAO{

	@Inject
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public int insertUserInfo(HashMap<String, Object> params) {
		IndexMapper mapper = sqlSession.getMapper(IndexMapper.class);
		return mapper.insertUserInfo(params);
	}

	@Override
	public List<CaseInsensitiveMap> selectUserInfo(HashMap<String, Object> params) {
		IndexMapper mapper = sqlSession.getMapper(IndexMapper.class);
		return mapper.selectUserInfo(params);
	}

	@Override
	public List<CaseInsensitiveMap> selectMenuInfo(HashMap<String, Object> params) {
		IndexMapper mapper = sqlSession.getMapper(IndexMapper.class);
		return mapper.selectMenuInfo(params);
	}

	@Override
	public int insertUserAccessInfo(UserAccessInfo userAccessInfo, HttpSession session, StringRedisTemplate redisTemplate) {
		IndexMapper mapper = sqlSession.getMapper(IndexMapper.class);
		
		try {
			if(userAccessInfo.getAccess_ip().equals("127.0.0.1") || userAccessInfo.getAccess_ip().equals("localhost"))
				return 0;
			
			LoginInfo loginInfo = SessionUtil.getInstance().getLoginInfo(session, redisTemplate);
			
			if(loginInfo != null) {
				
				if(loginInfo.user_cd != null) {
					userAccessInfo.setUser_cd(loginInfo.user_cd);
					userAccessInfo.setIs_success(1);
				}else{
					userAccessInfo.setUser_cd(0);
					userAccessInfo.setIs_success(0);
				}
			}else {
				userAccessInfo.setUser_cd(0);
				userAccessInfo.setIs_success(0);
			}
			
			mapper.insertUserAccessInfo(userAccessInfo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int updateLastLoginInfo(HashMap<String, Object> params) {
		IndexMapper mapper = sqlSession.getMapper(IndexMapper.class);
		return mapper.updateLastLoginInfo(params);
	}

}
