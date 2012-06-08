package com.appcelerator.cloud.sdk.oauth2;


public class Cocoafish2Error extends Throwable {

    private static final long serialVersionUID = 1L;

    private int mErrorCode = 0;
    private String mErrorType;

    public Cocoafish2Error(String message) {
        super(message);
    }

    public Cocoafish2Error(String message, String type, int code) {
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