package com.appcelerator.cloud.pushdemo;

import org.json.JSONException;
import org.json.JSONObject;

import com.appcelerator.cloud.push.PushService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CustomReceiver extends BroadcastReceiver{
	public final static String LOG_TAG = CustomReceiver.class.getName();
	private final static String MSG_ARRIVAL = PushService.ACTION_MSG_ARRIVAL;

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null || context == null)
			return;
		
		if (intent.getAction().equals(MSG_ARRIVAL)){
			String payloadStr = intent.getStringExtra("payload");
			
			// Covert payload from String to JSONObject
			JSONObject payload = null;
			try {
				payload = new JSONObject(payloadStr);
			} catch (JSONException ex) {
			}
			showNotification(context, payload);
		}
	}
	
	private void showNotification(Context context, JSONObject payload){
		if (payload == null) {
			Log.e(LOG_TAG, "Payload is null!");
		}

		// Ensure payload is correct, and get needed information from received payload
		String alert = null;
		try {
			JSONObject androidPartJson = payload.getJSONObject("android");
			alert = androidPartJson.optString("alert", null);;
		} catch (JSONException ex) {
			if(alert==null) alert = payload.toString();
		}

		NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification();

		n.flags |= Notification.FLAG_SHOW_LIGHTS;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.icon = R.drawable.icon;
		n.defaults = Notification.DEFAULT_ALL;
		
		String title = context.getResources().getString(R.string.app_name);
		Intent intent = new Intent(context, ArrivalActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("payload", payload.toString());
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		n.setLatestEventInfo(context, title + " (Custom)", alert, pi);
		notifManager.notify(0, n);
	}
}