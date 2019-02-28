package yusq.hyperledger.example.demo;

import yusq.hyperledger.example.support.DtoModel;

/**
 * 通用返回结果类
 *
 * @param <T>
 * @author pengpeng
 * @version 1.0
 * @date 2014年6月13日 上午8:59:37
 */
public class Result<T> implements DtoModel {

	private static final long serialVersionUID = 1L;

	/** 成功与否 */
    private boolean success;

    /** 结果代码 */
    private String code;
    
    /** 消息 */
    private String message;
    
    /** 结果数据 */
    private T data;
    
	Result() {
	}

	Result(boolean success, String code, String message, T data) {
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	Result(Result<T> result) {
		this.success = result.isSuccess();
		this.code = result.getCode();
		this.message = result.getMessage();
		this.data = result.getData();
	}

	public boolean isSuccess() {
		return success;
	}

	protected void setSuccess(boolean success) {
		this.success = success;
	}

	public String getCode() {
		return code;
	}

	protected void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}
	
	protected void setData(T data) {
		this.data = data;
	}

	public T get() {
		if(success) {
			return data;
		} else {
			String message = this.message;
			if(message == null || message.trim().equals("")) {
				message = "Unkown Error";
			}
		}
		return null;
	}
	
	public static Builder success() {
		return new Builder(Boolean.TRUE);
	}
	
	public static Builder failure() {
		return new Builder(Boolean.FALSE);
	}
	
	@Override
	public String toString() {
		return "Result [success=" + success + ", code=" + code + ", message="
				+ message + ", data=" + data + "]";
	}
	
	public static class Builder {
		
	    private boolean success = true;

	    private String code = "200";
	    
	    private String message;
	    
	    private Object data;

		Builder(boolean success) {
			this.success = success;
		}
		
		public Builder code(String code) {
			this.code = code;
			return this;
		}
		
		public Builder message(String message) {
			this.message = message;
			return this;
		}
		
		public Builder data(Object data) {
			this.data = data;
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public <T> Result<T> build() {
			return new Result<T>(success, code, message, (T) data);
		}
	    
	}
	
}
