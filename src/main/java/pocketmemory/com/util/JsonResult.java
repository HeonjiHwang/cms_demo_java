package pocketmemory.com.util;

public class JsonResult {
	private boolean isSuccess = false;
	private Integer resultCode = null;
	private Object data = null;
	private String errorMessage = null;
	
	public boolean IsSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Integer getResultCode() {
		return resultCode;
	}
	public void setResultCode(Integer resultCode) {
		this.resultCode = resultCode;
	}
	public Object getData() {
		return this.data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
