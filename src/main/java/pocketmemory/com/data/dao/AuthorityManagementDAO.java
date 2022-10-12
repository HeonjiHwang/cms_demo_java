package pocketmemory.com.data.dao;

import java.util.HashMap;

import org.springframework.stereotype.Repository;

@Repository("AuthorityManagementDAO")
public interface AuthorityManagementDAO {
	int insertAuthorityInfo(HashMap<String, Object> params);
	
	int updateAuthorityInfo(HashMap<String, Object> params);
	
	int deleteAuthorityInfo(HashMap<String, Object> params);
}
