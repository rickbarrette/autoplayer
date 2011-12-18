/**
 * HeadphoneListenerService.java
 * @date Jul 20, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.AutoPlayer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * This only purpose of this service is to keep HeadPhoneReceiver.java a registered reciever
 * @author ricky
 */
public class HeadphoneListenerService extends Service {

	@Override
	public void onCreate() {
		this.registerReceiver(new HeadPhoneReceiver(), new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return START_STICKY;
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 * @author ricky barrette
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
