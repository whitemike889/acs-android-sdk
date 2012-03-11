package com.cocoafish.demo;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.appcelerator.cloud.sdk.Cocoafish;
import com.cocoafish.test.TestDriver;

/**
 * This is an example of a {@link android.app.Application} class.  Ordinarily you would use
 * a class like this as a central repository for information that might be shared between multiple
 * activities.
 * 
 * In this case, we have not defined any specific work for this Application.
 * 
 * See samples/ApiDemos/tests/src/com.example.android.apis/ApiDemosApplicationTests for an example
 * of how to perform unit tests on an Application object.
 */
public class DemoApplication extends Application {
	// public static final String APP_ID = "yfqXvb0AClkrVE2mbgmqJmB17BEcEKzF"; // app id in cocoafish.org
	public static final String APP_ID = "8ivA6UgS8l29suqphNAJYT58V9NPwwZr";
	public static final String FACEBOOK_APP_ID = "";
	public static final String APP_COMSUMER_KEY = "mXIynNB5BDWR1WS5X2ABDA3P1U31Hk8o";
	public static final String APP_COMSUMER_SECRET = "sfyc9Oozk0IABkouWDxF9IPzpBfbP1c0";
	private static Cocoafish sdk = null;
	private static DemoSession session = null;


	@Override
    public void onCreate() {
        /*
         * This populates the default values from the preferences XML file. See
         * {@link DefaultValues} for more details.
         */
        PreferenceManager.setDefaultValues(this, R.xml.default_values, false);
        
        // initialize Cocoafish
        initialize(APP_COMSUMER_KEY, APP_COMSUMER_SECRET, getApplicationContext());
        
        // Apply for a registration_id from c2dm server.
        // The registration_id should be stored in cocoafish's server,
        // afterwards, cocoafish can send push notification to this program.
        registerC2DM();
    }

    private static void initialize(String appComsumerKey, String appComsumerSecret, Context appContext ) {
		sdk = new Cocoafish(appComsumerKey, appComsumerSecret, appContext);
		
//		TestDriver d = new TestDriver();
//		d.testSDK();
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
    
	public void registerC2DM(){
		C2DMessaging.register(this, "paul@cocoafish.com");
	}

}
