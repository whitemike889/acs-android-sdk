package com.appcelerator.cloud.sdk.oauth2;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.appcelerator.cloud.sdk.CCConstants;
import com.appcelerator.cloud.sdk.CCMeta;
import com.appcelerator.cloud.sdk.CCRequestMethod;
import com.appcelerator.cloud.sdk.CCResponse;
import com.appcelerator.cloud.sdk.CCUser;
import com.appcelerator.cloud.sdk.Cocoafish;
import com.appcelerator.cloud.sdk.CocoafishError;

//will reuse sendRequest method of Cocoafish
//Cocoafish2 constructors don't expect appKey supported by Cocoafish. It overrides the constructors from Cocoafish 
//which accept appKey to treat the parameter as OAuth consumer key. 
//Please note that passing OAuth consumer key only will not be able to construct an OAuthConsumer object. So it's 
//not possible to sign requests.
public class Cocoafish2 extends Cocoafish {

    // Strings used in the authorization flow
	public static final String REDIRECT_URI = "acsconnect://success";
    public static final String CANCEL_URI = "acsconnect://cancel";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCESS_TOKEN_EXPIRES_IN = "expires_in";
    public static final String ACTION_LOGIN = "oauth";
    public static final String ACTION_SINGUP = "signup";
    
    private String authHost = CCConstants.DEFAULT_AUTH_HOST;
	private String clientId = null;			//OAuth consumer key
    private String accessToken = null;
    private long accessExpires = 0;
    private DialogListener customDialogListener;
    private DlgCustomizer dlgCustomizer; 
    


	/**
     * Constructor for Cocoafish2 object.
     * Note: SDK will fail to sign requests without OAuth consumer secret.
     */
	public Cocoafish2(String consumerKey) {
		super(null); //construct Cocoafish object with null appKey
		this.clientId = consumerKey;
	}

	/**
     * Note: SDK will fail to sign requests without OAuth consumer secret.
	 * @param consumerKey
	 * @param context
	 */
	public Cocoafish2(String consumerKey, Context context) {
		super(null, context); //construct Cocoafish object with null appKey
		this.clientId = consumerKey;
	}

	/**
     * Note: SDK will fail to sign requests without OAuth consumer secret.
	 * @param consumerKey
	 * @param context
	 * @param hostname
	 */
	public Cocoafish2(String consumerKey, Context context, String hostname) {
		super(null, context, hostname); //construct Cocoafish object with null appKey
		this.clientId = consumerKey;
	}

	public Cocoafish2(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
		this.clientId = consumerKey;
	}

	public Cocoafish2(String consumerKey, String consumerSecret, Context context) {
		super(consumerKey, consumerSecret, context);
		this.clientId = consumerKey;
	}

	public Cocoafish2(String consumerKey, String consumerSecret, Context context, String hostname) {
		super(consumerKey, consumerSecret, context, hostname);
		this.clientId = consumerKey;
	}
    
    
    
    /**
     * Default authorize method. Grants only basic permissions.
     * See authorize() below for @params.
     */
    public void authorize(Activity activity, String action, final DialogListener listener) {
        authorize(activity, action, new String[] {}, listener, false);
    }
    
    
    
    /**
     * Default authorize method. Grants only basic permissions.
     * See authorize() below for @params.
     */
    public void authorize(Activity activity, String action, final DialogListener listener, boolean useSecure) {
        authorize(activity, action, new String[] {}, listener, useSecure);
    }


    /**
     * 
     * @param activity
     * @param permissions
     * @param listener
     */
    public void authorize(Activity activity, String action, String[] permissions, final DialogListener listener, boolean useSecure) {
    	customDialogListener = listener;
        startDialogAuth(activity, action, permissions, useSecure);
    }

    
	private void startDialogAuth(Activity activity, String action, String[] permissions, boolean useSecure) {
		final String method = "Cocoafish2.startDialogAuth";
		
        Bundle params = new Bundle();
        if (permissions.length > 0) {
            params.putString("scope", TextUtils.join(",", permissions));
        }
        
        CookieSyncManager.createInstance(activity);
        
        dialog(activity, action, params, new DialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                setAccessToken(values.getString(ACCESS_TOKEN));
                setAccessExpiresIn(values.getString(ACCESS_TOKEN_EXPIRES_IN));
                setAppKey(values.getString("key")); //see method setAppKey for note
                if (isSessionValid()) {
                    Log.d(method, "Login Success! access_token=" + getAccessToken() + " expires=" + getAccessExpires());
                    //made a call to get user information to satisfy Cocoafish
    				try {
						CCResponse response = Cocoafish2.super.sendRequest("users/show/me.json", CCRequestMethod.GET, null, false);
						updateSessionInfo(response);
					} catch (Throwable e) {
						Log.d(method, "Failed to get user information: " + e.getMessage());
						customDialogListener.onCocoafish2Error(new Cocoafish2Error(e.getLocalizedMessage()));
					} 

                    customDialogListener.onComplete(values);
                } else {
                	customDialogListener.onCocoafish2Error(new Cocoafish2Error("Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Log.d(method, "Login failed: " + error);
                customDialogListener.onError(error);
            }

            public void onCocoafish2Error(Cocoafish2Error error) {
                Log.d(method, "Login failed: " + error);
                customDialogListener.onCocoafish2Error(error);
            }

            public void onCancel() {
                Log.d(method, "Login canceled");
                customDialogListener.onCancel();
            }
        }, useSecure);
    }

	
	private void updateSessionInfo(CCResponse response) throws CocoafishError {
		if (response != null && response.getMeta() != null) {
			CCMeta meta = response.getMeta();
			if (meta.getCode() == CCConstants.SUCCESS_CODE) {
				try {
					if (response.getResponseData() != null) {
						JSONArray array = response.getResponseData().getJSONArray(CCConstants.USERS);
						if (array != null && array.length() > 0) {
							currentUser = new CCUser(array.getJSONObject(0));
							saveSessionInfo();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

    /**
     * Invalidate the current user session by removing the access token in memory, clearing the browser cookie, and invalidating 
     * the access token by sending request to authorization server.
     *
     * Note that this method blocks waiting for a network response, so do not call it in a UI thread.
     *
     * @param context The Android context in which the logout should be called: it should be the same context in which the login 
     * 			occurred in order to clear any stored cookies
     * @throws IOException
     * @throws MalformedURLException
     * @return JSON string representation of the oauth/invalidate response ("true" if successful)
     */
    public String logout(Context context, boolean useSecure) throws MalformedURLException, IOException {

    	StringBuffer endpoint = null;
		if (useSecure) {
			endpoint = new StringBuffer(CCConstants.HTTPS_HEAD);
		} else {
			endpoint = new StringBuffer(CCConstants.HTTP_HEAD);
		}
		endpoint.append(this.authHost);
		endpoint.append("/oauth/invalidate");
        Bundle parameters = new Bundle();
        if (isSessionValid()) {
            parameters.putString(ACCESS_TOKEN, getAccessToken());
        } else {
			Toast.makeText( context, "session invalid: no access token", Toast.LENGTH_SHORT).show();
			return null;
        }
        
        //String url = endpoint + "?" + Util.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Util.showAlert(context, "Error", "Application requires permission to access the Internet");
            return null;
        } else {
            String response = request(endpoint.toString(), parameters, "GET");
            super.clearSessionInfo();
            setAccessToken(null);
            setAccessExpires(0);
            setAppKey(null);
            return response;
        } 	

    }



    public String request(String url, Bundle params, String httpMethod) throws MalformedURLException, IOException {
        return Util.openUrl(url, httpMethod, params);
    }

    
    public void dialog(Context context, String action, DialogListener listener, boolean useSecure) {
        dialog(context, action, new Bundle(), listener, useSecure);
    }

    /**
     * Show dialog for authentication
     * @param context
     * @param action
     * @param parameters
     * @param listener
     */
    public void dialog(Context context, String action, Bundle parameters, final DialogListener listener, boolean useSecure) {

    	StringBuffer endpoint = null;
		if (useSecure) {
			endpoint = new StringBuffer(CCConstants.HTTPS_HEAD);
		} else {
			endpoint = new StringBuffer(CCConstants.HTTP_HEAD);
		}
		
    	endpoint.append(this.authHost);
        parameters.putString("client_id", clientId);
        parameters.putString("redirect_uri", REDIRECT_URI);

        if(ACTION_LOGIN.equals(action)) {
	    	endpoint.append("/oauth/authorize");
	        parameters.putString("response_type", "token");
		} else {
			endpoint.append("/users/sign_up");
		}

        if (isSessionValid()) {
            parameters.putString(ACCESS_TOKEN, getAccessToken());
        }
        endpoint.append("?");
        endpoint.append(Util.encodeUrl(parameters));
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Util.showAlert(context, "Error", "Application requires permission to access the Internet");
        } else {
            new Cocoafish2Dialog(context, endpoint.toString(), listener, this.dlgCustomizer).show();
        }
    }

    /**
     * @return boolean - whether this object has an non-expired session token
     */
    public boolean isSessionValid() {
//        return (getAccessToken() != null) &&
//                ((getAccessExpires() == 0) ||
//                        (System.currentTimeMillis() < getAccessExpires()));
    	return (getAccessToken() != null);
    }

    
    //This is to set appKey for super class to send to server for backward compatibility
    //Load balancer expects appKey 
    public void setAppKey(String appKey) {
    	super.appKey = appKey;
    }
    
    public String getAppKey() {
    	return super.appKey;
    }
    
    
    /**
     * Retrieve the OAuth 2.0 access token for API access: treat with care.
     * Returns null if no session exists.
     *
     * @return String - access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Retrieve the current session's expiration time (in milliseconds since
     * Unix epoch), or 0 if the session doesn't expire or doesn't exist.
     *
     * @return long - session expiration time
     */
    public long getAccessExpires() {
        return accessExpires;
    }

    /**
     * Set the OAuth 2.0 access token for API access.
     *
     * @param token - access token
     */
    public void setAccessToken(String token) {
        accessToken = token;
    }

    /**
     * Set the current session's expiration time (in milliseconds since Unix
     * epoch), or 0 if the session doesn't expire.
     *
     * @param time - timestamp in milliseconds
     */
    public void setAccessExpires(long time) {
        accessExpires = time;
    }

    /**
     * Set the current session's duration (in seconds since Unix epoch).
     *
     * @param expiresIn - duration in seconds
     */
    public void setAccessExpiresIn(String expiresIn) {
        if (expiresIn != null && !expiresIn.equals("0")) {
            setAccessExpires(System.currentTimeMillis() + Integer.parseInt(expiresIn) * 1000);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String appId) {
    	clientId = appId;
    }

    public String getAuthHost() {
		return authHost;
	}

	public void setAuthHost(String authHost) {
		this.authHost = authHost;
	}

    public DlgCustomizer getDlgCustomizer() {
		return dlgCustomizer;
	}

	public void setDlgCustomizer(DlgCustomizer dlgCustomizer) {
		this.dlgCustomizer = dlgCustomizer;
	}
	
	
    /**
     * Callback interface for dialog requests.
     *
     */
    public static interface DialogListener {

        /**
         * Called when a dialog completes.
         *
         * Executed by the thread that initiated the dialog.
         *
         * @param values
         *            Key-value string pairs extracted from the response.
         */
        public void onComplete(Bundle values);

        /**
         * Called when a response to a dialog with an error.
         *
         * Executed by the thread that initiated the dialog.
         *
         */
        public void onCocoafish2Error(Cocoafish2Error e);

        /**
         * Called when a dialog has an error.
         *
         * Executed by the thread that initiated the dialog.
         *
         */
        public void onError(DialogError e);

        /**
         * Called when a dialog is canceled by the user.
         *
         * Executed by the thread that initiated the dialog.
         *
         */
        public void onCancel();

    }

}
