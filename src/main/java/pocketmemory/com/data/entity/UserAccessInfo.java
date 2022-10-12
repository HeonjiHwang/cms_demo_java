package pocketmemory.com.data.entity;

public class UserAccessInfo {
	private Integer user_cd = null;
	private String menu = null;
	private String action_type = null;
	private Integer is_success = null;
	private String access_ip = null;
	
	public String getAccess_ip() {
		return access_ip;
	}
	public void setAccess_ip(String access_ip) {
		this.access_ip = access_ip;
	}
	public Integer getUser_cd() {
		return user_cd;
	}
	public void setUser_cd(Integer user_cd) {
		this.user_cd = user_cd;
	}
	public String getMenu() {
		return menu;
	}
	public void setMenu(String menu) {
		this.menu = menu;
	}
	public String getAction_type() {
		return action_type;
	}
	public void setAction_type(String action_type) {
		this.action_type = action_type;
	}
	public Integer getIs_success() {
		return is_success;
	}
	public void setIs_success(Integer is_success) {
		this.is_success = is_success;
	}
}
