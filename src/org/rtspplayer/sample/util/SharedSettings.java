/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SharedSettings 
{
	final public static String TAG="SharedSettings";
	
	// misc
	public  boolean AllowFullscreenMode = true;
	public  int LockPlayerViewOrientation = 0; 				// 0 - unlock, 1 - Landscape, 2 - Portrait, 3 - current
	public  boolean AllowPlayStreamsSequentially = true;

	// connection
	public  int connectionProtocol 		= -1;				// 0 - udp, 1 - tcp, 2 - http, 3 - https, -1 - AUTO
	public  int connectionDetectionTime = 3000;				// in milliseconds
	public  int connectionBufferingTime = 1000;				// in milliseconds

	// decoder
	public  int decoderType = 1;							// 0 - soft, 1 - hard stagefright
	public  int decoderNumberOfCpuCores = 0;    			// <=0 - autodetect and use, > 0 - manually set

	// renderer
	public  int rendererType = 1;							// 0 - SDL, 1 - pure OpenGL
	public  int rendererEnableColorVideo = 1; 				// 0 - grayscale, 1 - color
	public  int rendererEnableAspectRatio = 1; 				// 0 - resize, 1 - aspect
	public  int rendererAspectRatioMode = 1;				// 0 - stretch, 1 - fittoscreen with aspect ratio
    														// 2 - crop, 3 - 100% size, 4 - zoom mode
	public  int	rendererAspectRatioZoomModePercent	= 100;	// value in percents 
	public  int	rendererAspectRatioMoveModeX		= 50;	// 50 - center 
	public  int	rendererAspectRatioMoveModeY		= 50;	// 50 - center 
	
	// synchro
	public  int synchroEnable = 1;							// enable audio video synchro
	public  int synchroNeedDropVideoFrames = 0;				// drop video frames if it older
	
	public  int selectedTabNum = 0;				// 
	public  int savedTabNumForSavedId = 0;		// 
	
	// misc
	public  int thumbnailThreadCount = 2;
	public  boolean showPreview = true;
	
	private  Context m_Context = null;
	private  SharedPreferences settings = null;
	private  SharedPreferences.Editor editor = null;
	
	private static volatile SharedSettings _inst = null;
	private SharedSettings()
	{
		m_Context = null;
	}

	private SharedSettings(final Context mContext)
	{
		m_Context = mContext;
	}
	
	public static synchronized SharedSettings getInstance(final Context mContext)
	{
		if (_inst == null)
		{
			_inst = new SharedSettings(mContext);
			_inst.loadPrefSettings();
			_inst.savePrefSettings();
			Log.e(TAG, "instance created.");
		}
		
		return _inst;
	}

	public static synchronized SharedSettings getInstance()
	{
		return _inst;
	}
	
 	public void loadPrefSettings() 
	{
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
 		AllowFullscreenMode = settings.getBoolean( "AllowFullscreenMode", true);
 		LockPlayerViewOrientation = settings.getInt("LockPlayerViewOrientation", 0);
 		AllowPlayStreamsSequentially = settings.getBoolean( "AllowPlayStreamsSequentially", true);

 		connectionProtocol = settings.getInt("connectionProtocol", -1);
 		connectionDetectionTime = settings.getInt("connectionDetectionTime", 3000);
 		connectionBufferingTime = settings.getInt("connectionBufferingTime", 1000);
 		
 		decoderType = settings.getInt("decoderType", 1);
 		//decoderNumberOfCpuCores = settings.getInt("decoderNumberOfCpuCores", 1);
 		rendererType = settings.getInt("rendererType", 1);
 		rendererEnableColorVideo = settings.getInt("rendererEnableColorVideo", 1);
 		rendererEnableAspectRatio = settings.getInt("rendererEnableAspectRatio", 1);
 		rendererAspectRatioMode = settings.getInt("rendererAspectRatioMode", 1);
 		//rendererAspectRatioZoomModePercent = settings.getInt("rendererAspectRatioZoomModePercent", 100);
 		
 		
 		synchroEnable = settings.getInt("synchroEnable", 1);
 		synchroNeedDropVideoFrames = settings.getInt("synchroNeedDropVideoFrames", 0);
 		
 		selectedTabNum = settings.getInt("selectedTabNum", 0);
 		savedTabNumForSavedId = settings.getInt("savedTabNumForSavedId", 0);
 		
 		thumbnailThreadCount = settings.getInt("thumbnailThreadCount", 2);
 		showPreview = settings.getBoolean("showPreview", true);
 		
		Log.e(TAG, "Settings loaded." + selectedTabNum);
	}

	public void savePrefSettings() 
	{
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putBoolean("AllowFullscreenMode", AllowFullscreenMode);
 		editor.putInt("LockPlayerViewOrientation", LockPlayerViewOrientation);
 		editor.putBoolean("AllowPlayStreamsSequentially", AllowPlayStreamsSequentially);
 		
 		editor.putInt("connectionProtocol", connectionProtocol);
 		editor.putInt("connectionDetectionTime", connectionDetectionTime);
 		editor.putInt("connectionBufferingTime", connectionBufferingTime);
 		
 		editor.putInt("decoderType", decoderType);
 		//editor.putInt("decoderNumberOfCpuCores", decoderNumberOfCpuCores);
 		editor.putInt("rendererType", rendererType);
 		editor.putInt("rendererEnableColorVideo", rendererEnableColorVideo);
 		editor.putInt("rendererEnableAspectRatio", rendererEnableAspectRatio);
 		editor.putInt("rendererAspectRatioMode", rendererAspectRatioMode);
 		//editor.putInt("rendererAspectRatioZoomModePercent", rendererAspectRatioZoomModePercent);
 		
 		editor.putInt("synchroEnable", synchroEnable);
 		editor.putInt("synchroNeedDropVideoFrames", synchroNeedDropVideoFrames);

 		editor.putInt("selectedTabNum", selectedTabNum);
 		editor.putInt("savedTabNumForSavedId", savedTabNumForSavedId);
 		
 		editor.putInt("thumbnailThreadCount", thumbnailThreadCount);
 		editor.putBoolean("showPreview", showPreview);
		
		editor.commit();
		Log.e(TAG, "Settings saved." + selectedTabNum);
	}
	
	public boolean getBooleanValueForKey(final String key) 
	{
 		if (key.isEmpty())
 			return false;
 		
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		return settings.getBoolean(key, false);
	}
	
 	public void setBooleanValueForKey(final String key, final boolean value) 
	{
 		if (key.isEmpty())
 			return;
 		
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putBoolean(key, value);
		editor.commit();
	}

 	public long getLongValueForKey(final String key) 
	{
 		if (key.isEmpty())
 			return 0;
 		
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		return settings.getLong(key, 0);
	}
	
 	public void setLongValueForKey(final String key, final long value) 
	{
 		if (key.isEmpty())
 			return;
 		
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putLong(key, value);
		editor.commit();
	}
 	
 	public int getIntValueForKey(final String key) 
	{
 		if (key.isEmpty())
 			return 0;
 		
		// load preferences settings to local variables
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		return settings.getInt(key, 0);
	}
	
 	public void setIntValueForKey(final String key, final int value) 
	{
 		if (key.isEmpty())
 			return;
 		
 		if (settings == null)
 			settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
 		
		// save preferences settings
 		if (editor == null)
 			editor = settings.edit();

 		editor.putInt(key, value);
		editor.commit();
	}
	
}