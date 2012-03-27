package com.appcelerator.cloud.pushdemo;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.appcelerator.cloud.push.CCPushService;
import com.appcelerator.cloud.push.PushBroadcastReceiver;

public class ExtendedReceiver extends PushBroadcastReceiver {
	public final static String LOG_TAG = PushBroadcastReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null)
			return;

		if (!CCPushService.getInstance().ifEnabled(context))
			return;

		if (intent.getAction().equals(MSG_ARRIVAL)) {
			String payloadStr = intent.getStringExtra("payload");
			// Convert payload from String to JSONObject
			JSONObject payload = null;
			try {
				payload = new JSONObject(payloadStr);
				JSONObject payloadAndroidPart = payload.getJSONObject("android");
				String alert = payloadAndroidPart.optString("alert") + " (Extend)";
				payloadAndroidPart.remove("alert");
				payloadAndroidPart.put("alert", alert);
				payload.remove("android");
				payload.put("android", payloadAndroidPart);
				
			} catch (JSONException ex) {
			}
			showNotification(context, payload, 0);
			// showNotification(context, payload);
		}
	}
}