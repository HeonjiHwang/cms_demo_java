package pocketmemory.com.data.mapper;

import java.util.HashMap;

public interface AuthorityManagementMapper {
	int insertAuthorityInfo(HashMap<String, Object> params);
	
	int updateAuthorityInfo(HashMap<String, Object> params);
	
	int deleteAuthorityInfo(HashMap<String, Object> params);
}
