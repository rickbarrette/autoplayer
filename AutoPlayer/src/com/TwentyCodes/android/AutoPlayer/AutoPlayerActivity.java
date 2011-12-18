/**
 * AutoPlayerActivity.java
 * @author ricky barrette
 * @author Twenty Codes, LLC
 * @date July 20, 2011
 */
package com.TwentyCodes.android.AutoPlayer;

import java.net.URISyntaxException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.TwentyCodes.android.exception.ExceptionHandler;
import com.TwentyCodes.android.exception.R;

/**
 * This activity will be used to allow the user to pick an activity, and a set a volume level to be started/applied when a headphone is pluged in
 * @author ricky
 */
public class AutoPlayerActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, OnCheckedChangeListener {
    public static final String SETTINGS = "setings";
	public static final String KEY_APP = "app";
	public static final String KEY_VOLUME = "volume";
	public static final String KEY_CONFIRM = "confirm";
	public static final String KEY_ENABLED = "enabled";
	private SharedPreferences mSettings;
	private ImageView mAppIcon;
	private ProgressDialog mProgress;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.mSettings = this.getSharedPreferences(SETTINGS, Context.MODE_WORLD_WRITEABLE);

        /*
         * toggle button
         */
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setChecked(mSettings.getBoolean(KEY_ENABLED, true));
        toggle.setOnCheckedChangeListener(this);
        
        /*
         * confirmation check box
         */
        CheckBox confimationCheckBox = (CheckBox) findViewById(R.id.confirmCheckBox);
        confimationCheckBox.setOnCheckedChangeListener(this);
        confimationCheckBox.setChecked(mSettings.getBoolean(KEY_CONFIRM, false));
        
        /*
         * start the service if enabled
         */
        if(toggle.isChecked());
        	this.startService(new Intent(this, HeadphoneListenerService.class));

    	/*
    	 * music volume
    	 */
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        SeekBar volume = (SeekBar) findViewById(R.id.music_volume);
        volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setProgress(mSettings.getInt(KEY_VOLUME, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
        volume.setOnSeekBarChangeListener(this);
        
        /*
         * application icon
         */
        mAppIcon = (ImageView) findViewById(R.id.app_icon);
        try {
			mAppIcon.setBackgroundDrawable(getPackageManager().getActivityIcon(Intent.getIntent(this.mSettings.getString(AutoPlayerActivity.KEY_APP, null))));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}

        /*
         * application picker button
         */
        findViewById(R.id.activty_picker_button).setOnClickListener(this);
        
        /*
         * Version information textview
         */
        TextView version = (TextView) findViewById(R.id.version);
		PackageManager pm = getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException eNnf) {
			//doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.versionName = "unknown";
			pi.versionCode = 1;
		}
		version.setText(getText(R.string.version)+" "+pi.versionName+" "+getString(R.string.build)+" "+pi.versionCode);
        
    }

    /**
     * called when the application picker button is clicked
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		mProgress = ProgressDialog.show(this, "", getString(R.string.loading), true, true);
		
		findViewById(R.id.activty_picker_button).setEnabled(false);
		/// Pick an application
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		 
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
		startActivityForResult(pickIntent, 0);
	}
		 
	/**
	 * called when the application picker is finished
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(mProgress != null)
			if(mProgress.isShowing())
				mProgress.dismiss();
		
		findViewById(R.id.activty_picker_button).setEnabled(true);
	   if (data != null) {
		   this.mSettings.edit().putString(KEY_APP, data.toURI()).commit();
		   
		   try {
				mAppIcon.setBackgroundDrawable(getPackageManager().getActivityIcon(data));
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			} catch (NullPointerException e){
				e.printStackTrace();
			}
	   }
	}

	/**
	 * called when the music volume setting is adjusted
	 * (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	   this.mSettings.edit().putInt(KEY_VOLUME, progress).commit();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// NOT USED
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// NOT USED
	}

	/**
	 * called when the main toggle or the confirmation check box is un/checked
	 * (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
			case R.id.confirmCheckBox:
				this.mSettings.edit().putBoolean(KEY_CONFIRM, isChecked).commit();
				break;
			case R.id.toggleButton:
				this.mSettings.edit().putBoolean(KEY_ENABLED, isChecked).commit();
				if(!isChecked)
					this.stopService(new Intent(this, HeadphoneListenerService.class));
				else
					this.startService(new Intent(this, HeadphoneListenerService.class));
				break;
		}
	}
}