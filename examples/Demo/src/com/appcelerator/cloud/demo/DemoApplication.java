package com.appcelerator.cloud.demo;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.appcelerator.cloud.sdk.Cocoafish;

public class DemoApplication extends Application {
	// TODO Update your own app_id here
	public static final String APP_ID = "ldfIk3IPNtcOY5BBKvolherePQcCzNUs";
	public static final String FACEBOOK_APP_ID = "";
	// TODO Update your own oAuth account here
	public static final String APP_CONSUMER_KEY = "uHibgpwMr0gYgZXMm67UHZNij48W1WXz";
	public static final String APP_CONSUMER_SECRET = "brggfwU0yoLGlLhDew8hqFiPfMQEV00s";
	private static Cocoafish sdk = null;
	private static DemoSession session = null;

	@Override
	public void onCreate() {
		PreferenceManager.setDefaultValues(this, R.xml.default_values, false);

		// Initialize Cocoafish
		initialize(APP_CONSUMER_KEY, APP_CONSUMER_SECRET, getApplicationContext());
	}

	private static void initialize(String appComsumerKey, String appComsumerSecret, Context appContext) {
		sdk = new Cocoafish(appComsumerKey, appComsumerSecret, appContext);
		session = new DemoSession();
	}

	@Override
	public void onTerminate() {
	}

	public static Cocoafish getSdk() {
		return sdk;
	}

	public static DemoSession getSession() {
		return session;
	}
}
