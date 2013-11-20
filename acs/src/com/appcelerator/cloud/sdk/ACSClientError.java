package com.appcelerator.cloud.sdk;

/**
 * Encapsulation of a ACS Error: a ACS request that could not be
 * fulfilled.
 * 
 */
public class ACSClientError extends Throwable {

	private static final long serialVersionUID = 1L;

	private int mErrorCode = 0;
	private String mErrorType;
	
	public ACSClientError(String message) {
		super(message);
	}

	public ACSClientError(String message, int code) {
		super(message);
		mErrorCode = code;
	}

    public ACSClientError(String message, String type, int code) {
        super(message);
        mErrorType = type;
        mErrorCode = code;
    }
    
	public int getErrorCode() {
		return mErrorCode;
	}

    public String getErrorType() {
        return mErrorType;
    }
}
