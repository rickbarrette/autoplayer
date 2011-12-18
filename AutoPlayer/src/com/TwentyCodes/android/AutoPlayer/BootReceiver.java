/**
 * BootReceiver.java
 * @date Jul 20, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.AutoPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This receiver will register HeadPhoneReceiver at boot
 * @author ricky
 */
public class BootReceiver extends BroadcastReceiver {

	/**
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 * @author ricky barrette
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(context.getSharedPreferences(AutoPlayerActivity.SETTINGS, Context.MODE_WORLD_WRITEABLE).getBoolean(AutoPlayerActivity.KEY_ENABLED, true));
			context.startService(new Intent(context, HeadphoneListenerService.class));
	}
}