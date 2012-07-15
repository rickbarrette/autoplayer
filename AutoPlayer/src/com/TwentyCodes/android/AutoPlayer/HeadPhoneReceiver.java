/**
 * HeadPhoneReceiver.java
 * @author ricky barrette
 * @author Twenty Codes, LLC
 * @date July 20, 2011
 */
package com.TwentyCodes.android.AutoPlayer;

import java.net.URISyntaxException;

import com.TwentyCodes.android.exception.ExceptionHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

/**
 * This receiver will handle head phone events
 * @author ricky
 */
public class HeadPhoneReceiver extends BroadcastReceiver {

	private static final String TAG = "HeadPhoneReceiver";
	private static final String LOG = "HeadPhoneReceiverLog";
	private static final String MUSIC_VOLUME = "music_volume";
	private AudioManager mAudioManager;
	private SharedPreferences mSettings;
	private Context mContext;

	/**
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, context));
		
		this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mSettings = context.getSharedPreferences(AutoPlayerActivity.SETTINGS, Context.MODE_WORLD_WRITEABLE);
		mContext = context;
		
		if(intent != null){
			Log.v(TAG,"Headphone event");
			if(intent.hasExtra("state")){
				Log.v(TAG,"Headphone event: "+ intent.getIntExtra("state", 0));
				if(intent.getIntExtra("state", 0) == 1){
					
					//check the event log, if this is the first event then continue 
					if(! mSettings.getBoolean(LOG, false)) {
						
						//log the event and current music volume
						mSettings.edit().putBoolean(LOG, true).putInt(MUSIC_VOLUME, this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)).commit();

						//Perform user's settings
						if(mSettings.getBoolean(AutoPlayerActivity.KEY_CONFIRM, false))
							context.startActivity(new Intent(context, ConfirmDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
						else
							applySettings();
					}
				} else {
					//restore previous volume
					setStreamVolume(mSettings.getInt(MUSIC_VOLUME, this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), AudioManager.STREAM_MUSIC);
					mSettings.edit().putBoolean(LOG, false).commit();
					/*
					 * TODO stop music activity
					 */
				}
			}
		}
	}
	
	/**
	 * Sets the music stream volume, and starts the user specified activity
	 * 
	 * @author ricky barrette
	 */
	private void applySettings() {
		try {
			setStreamVolume(mSettings.getInt(AutoPlayerActivity.KEY_VOLUME, this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), AudioManager.STREAM_MUSIC);
			mContext.getApplicationContext().startActivity(Intent.getIntent(mSettings.getString(AutoPlayerActivity.KEY_APP, null)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}

	/**
	 * set the volume of a particular stream
	 * @param volume
	 * @param stream
	 * @author ricky barrette
	 */
	private void setStreamVolume(int volume, int stream) {
		/*
		 * if the seek bar is set to a value that is higher than what the the stream value is set for
		 * then subtract the seek bar's value from the current volume of the stream, and then
		 * raise the stream by that many times 
		 */
		if (volume > mAudioManager.getStreamVolume(stream)) {
			int adjust = volume - mAudioManager.getStreamVolume(stream);
			for (int i = 0; i < adjust; i++) {
				mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE, stream, 0);
			}
		}
		
		/*
		 * if the seek bar is set to a value that is lower than what the the stream value is set for
		 * then subtract the current volume of the stream from the seek bar's value, and then
		 * lower the stream by that many times 
		 */
		if (volume < mAudioManager.getStreamVolume(stream)) {
			int adjust = mAudioManager.getStreamVolume(stream) - volume;
			for (int i = 0; i < adjust; i++) {
				mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, stream, 0);
			}
		}		
	}

}
