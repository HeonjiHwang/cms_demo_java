package pocketmemory.com.data.dao;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pocketmemory.com.data.mapper.AuthorityManagementMapper;

@Service("AuthorityManagementDAOImpl")
public class AuthorityManagementDAOImpl implements AuthorityManagementDAO{

	@Inject
	@Autowired
	SqlSession sqlSession;
	
	@Override
	public int insertAuthorityInfo(HashMap<String, Object> params) {
		AuthorityManagementMapper mapper = sqlSession.getMapper(AuthorityManagementMapper.class);
		return mapper.insertAuthorityInfo(params);
	}

	@Override
	public int updateAuthorityInfo(HashMap<String, Object> params) {
		AuthorityManagementMapper mapper = sqlSession.getMapper(AuthorityManagementMapper.class);
		return mapper.updateAuthorityInfo(params);
	}

	@Override
	public int deleteAuthorityInfo(HashMap<String, Object> params) {
		AuthorityManagementMapper mapper = sqlSession.getMapper(AuthorityManagementMapper.class);
		return mapper.deleteAuthorityInfo(params);
	}

}
