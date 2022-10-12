package pocketmemory.com.data.dao;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pocketmemory.com.data.mapper.UserManagementMapper;

@Service("UserManagementDAOImpl")
public class UserManagementDAOImpl implements UserManagementDAO {

	@Inject
	@Autowired
	SqlSession sqlSession;
	
	@Override
	public List<CaseInsensitiveMap> selectUsersInfo(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.selectUsersInfo(params);
	}

	@Override
	public int updateUserInfo(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.updateUserInfo(params);
	}

	@Override
	public int deleteUserInfo(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.deleteUserInfo(params);
	}

	@Override
	public List<CaseInsensitiveMap> selectAuthorityInfo() {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.selectAuthorityInfo();
	}

	@Override
	public List<CaseInsensitiveMap> checkIDValidation(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.checkIDValidation(params);
	}

	@Override
	public List<CaseInsensitiveMap> selectReqUserInfo() {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.selectReqUserInfo();
	}

	@Override
	public int insertReqUserInfo(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.insertReqUserInfo(params);
	}

	@Override
	public int insertReqUsertoUserInfo(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.insertReqUsertoUserInfo(params);
	}

	@Override
	public int deleteReqUserInfo(HashMap<String, Object> params) {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.deleteReqUserInfo(params);
	}

	@Override
	public List<CaseInsensitiveMap> selectUserAccessInfo() {
		UserManagementMapper mapper = sqlSession.getMapper(UserManagementMapper.class);
		return mapper.selectUserAccessInfo();
	}

}
