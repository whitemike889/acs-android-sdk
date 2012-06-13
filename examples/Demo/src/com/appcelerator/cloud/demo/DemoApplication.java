package com.appcelerator.cloud.demo;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.appcelerator.cloud.sdk.Cocoafish;
import com.appcelerator.cloud.sdk.oauth2.Cocoafish2;

public class DemoApplication extends Application {
	// TODO Update your own app_id here
	public static final String APP_ID = "NIE4y3ax2UnmiWtkpi3Rbr9RChBvit2a";
	public static final String FACEBOOK_APP_ID = "";
	// TODO Update your own oAuth account here
	public static final String APP_CONSUMER_KEY = "VGJSVgFHs7FaOcgcvMWMAGe6bwNpHBfq";
	public static final String APP_CONSUMER_SECRET = "ZDkLBzlL28ISUngLgjwuUuMdMqF3Jrm5";
	private static Cocoafish sdk = null;
	private static DemoSession session = null;

	@Override
	public void onCreate() {
		PreferenceManager.setDefaultValues(this, R.xml.default_values, false);

		// Initialize Cocoafish
		initialize(APP_CONSUMER_KEY, APP_CONSUMER_SECRET, getApplicationContext());
	}

	private static void initialize(String appComsumerKey, String appComsumerSecret, Context appContext) {
		//sdk = new Cocoafish(appComsumerKey, appComsumerSecret, appContext, "192.168.1.110:3000/v1/");
		sdk = new Cocoafish2(appComsumerKey, appComsumerSecret, appContext, "192.168.1.110:3000/v1/");
        ((Cocoafish2)sdk).setDlgCustomizer(new MyDlgCustomizer());

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
