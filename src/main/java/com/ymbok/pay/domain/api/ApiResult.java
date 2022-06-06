package com.ymbok.pay.domain.api;


import com.ymbok.pay.utils.GsonUtil;


public class ApiResult {
	
	/** 返回码：成功. */
    public static final int RESULT_OK = 0;
    
    /** 返回码：失败. */
    public static final int RESULT_ERROR = -1;

	/** 结果code. */
	private int resultCode;
	
	/** 结果 message. */
	private String resultMessage;
	
    /**
     * 构造.
     */
	public ApiResult() {
		super();
	}

	/**
	 * 构造.
	 *
	 * @param resultCode the result code
	 * @param resultMessage the result message
	 */
	public ApiResult(int resultCode, String resultMessage) {
		super();
		this.resultCode = resultCode;
		this.resultMessage = resultMessage;
	}
	
	/**
	 * 用json构造自己.
	 *
	 * @param json the json
	 */
	public ApiResult(String json) {
		super();
		ApiResult result = GsonUtil.fromJson(json, this.getClass());
		this.resultCode = result.getResultCode();
		this.resultMessage = result.getResultMessage();
	}

	/**
	 * 获取返回码.
	 *
	 * @return the result code
	 */
	public int getResultCode() {
		return resultCode;
	}

	/**
	 * 设置返回码.
	 *
	 * @param resultCode the new result code
	 */
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	/**
	 * 获取返回信息.
	 *
	 * @return the result message
	 */
	public String getResultMessage() {
		return resultMessage;
	}

	/**
	 * 设置返回信息.
	 *
	 * @param resultMessage the new result message
	 */
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	
	/**
	 * 描述：转换成json.
	 *
	 * @return the string
	 */
	public String toJson(){
		return GsonUtil.toJson(this);
	}
	

}
