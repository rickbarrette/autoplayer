/**
 * ConfirmDialog.java
 * @date Oct 7, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.AutoPlayer;

import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.TwentyCodes.android.exception.ExceptionHandler;
import com.TwentyCodes.android.exception.R;

/**
 * This is the confirmation dialog. it will allow the user to cancel auto player's actions for a specified amount of time.
 * @author ricky barrette
 */
public class ConfirmDialog extends Activity implements OnClickListener {
	
	/**
	 * Total period of this confirmation dialog
	 * This value is in seconds
	 */
	private static final byte PERIOD = 5;
	
	/**
	 * Handler message code for the current count of elapsed seconds
	 */
	protected static final int COUNT = 0;
	
	private AudioManager mAudioManager;
	private SharedPreferences mSettings;
	private ProgressBar mProgressBar;
	private Handler mHandler;
	
	/**
	 * This thread will be used as a timer. it will update the UI and apply the settings
	 */
	private Thread mTimer = new Thread(new Runnable(){
		
		@Override
		public void run() {
			int count = 0;
			boolean isCanceled = false;
			
			/*
			 * this is the timer function of this thread.
			 * it will wait for the period to elapse before continuing.
			 * it will update the UI every second
			 */
			while (count < PERIOD){	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					/*
					 * If this is caught, then the thread has been interrupted,
					 * we want to log this so we can handle it.
					 */
					e.printStackTrace();
					isCanceled = true;
				}
				count++;
				mHandler.sendMessage(mHandler.obtainMessage(COUNT, count));
			}
			
			if(!isCanceled){
			
				try {
					setStreamVolume(mSettings.getInt(AutoPlayerActivity.KEY_VOLUME, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), AudioManager.STREAM_MUSIC);
					getApplicationContext().startActivity(Intent.getIntent(mSettings.getString(AutoPlayerActivity.KEY_APP, null)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (NullPointerException e){
					e.printStackTrace();
				}
	
				/*
				 * the following is performed on the UI thread
				 */
				ConfirmDialog.this.runOnUiThread(new Runnable(){
	
					@Override
					public void run() {						
						ConfirmDialog.this.finish();
					}
				});
			}
		}
	});
	
	@Override
	public void onClick(View v) {
		mTimer.interrupt();
		this.finish();
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		this.setContentView(R.layout.confirm_dialog);
		this.mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.mSettings = this.getSharedPreferences(AutoPlayerActivity.SETTINGS, Context.MODE_WORLD_WRITEABLE);
		this.setTitle(R.string.app_name);
		
		/*
		 * cancel button
		 */
		findViewById(R.id.okButton).setOnClickListener(this);

		/*
		 * displays the icon of the application to be started
		 */
		ImageView appIcon = (ImageView) findViewById(R.id.app_icon);
        try {
			appIcon.setBackgroundDrawable(getPackageManager().getActivityIcon(Intent.getIntent(this.mSettings.getString(AutoPlayerActivity.KEY_APP, null))));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
        
        /*
         * displays the count down progress bar
         */
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(PERIOD);
        mProgressBar.setProgress(PERIOD);
        
        /*
         * prepare UI handler to handle updates from the timer thread.
         * once this is complete, start the timer thread
         */
        setUiHandler();
        mTimer.start();
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

	/**
	 * Sets up the UI handler to receive updates from the timer thread
	 * @author ricky barrette
	 */
	private void setUiHandler() {
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
					if(msg.what == COUNT){
						mProgressBar.setProgress( PERIOD - (Integer) msg.obj);
					}
			}
		};
	}

}