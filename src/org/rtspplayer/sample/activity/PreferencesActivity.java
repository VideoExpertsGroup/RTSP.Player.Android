/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.Preference;
import android.preference.PreferenceManager;
import java.nio.ByteBuffer;
import java.util.Map;

import org.rtspplayer.sample.R;


public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
    private SharedPreferences settings;
	private SharedPreferences.Editor editor;
    private CheckBoxPreference prefDecodingType;
    private CheckBoxPreference prefRendererType;
    private CheckBoxPreference prefShowPreview;

    private CheckBoxPreference prefSynchroEnable;
    private CheckBoxPreference prefVideoCheckSPS;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		String AppVersion="";
		String AppName = getResources().getString(R.string.app_name);
		PackageInfo pinfo;
		try 
		{
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
			AppVersion = pinfo.versionName;	
			AppName = (String) getText(R.string.app_name);
			setTitle(AppName + " " + AppVersion);
		} 
		catch (NameNotFoundException e1) 
		{
			e1.printStackTrace();
		}
		
		//Log.i("=onCreate AppVersion="+AppVersion+" AppName="+AppName);

		
		addPreferencesFromResource(R.xml.preferences);

		settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		prefRendererType  = (CheckBoxPreference) findPreference("rendererType");
		prefSynchroEnable = (CheckBoxPreference) findPreference("synchroEnable");
		
		findPreference("rendererEnableColorVideo").setEnabled(prefRendererType.isChecked());
		findPreference("synchroNeedDropVideoFrames").setEnabled(prefSynchroEnable.isChecked());
		
		prefShowPreview = (CheckBoxPreference) findPreference("showPreview");
		if (prefShowPreview != null)
			prefShowPreview.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);

		prefRendererType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
			public boolean onPreferenceChange(Preference pref, Object newValue) 
			{
				findPreference("rendererEnableColorVideo").setEnabled((Boolean)newValue);
				return true;
			}
		});

		prefSynchroEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
			public boolean onPreferenceChange(Preference pref, Object newValue) 
			{
				findPreference("synchroNeedDropVideoFrames").setEnabled((Boolean)newValue);
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() 
	{
	    super.onResume();
	
	    // Set up initial values for all list preferences
	    Map<String, ?> sharedPreferencesMap = getPreferenceScreen().getSharedPreferences().getAll();
	    Preference pref;
	    for (Map.Entry<String, ?> entry : sharedPreferencesMap.entrySet()) 
	    {
	        pref = findPreference(entry.getKey());
	        if (pref instanceof EditTextPreference) 
	        {
	        	EditTextPreference editPref = (EditTextPreference) pref;
	            String strCurr = pref.getSummary().toString();
	            if (strCurr.indexOf("\nCurrent value: ") >= 0)
	            {
	            	String oldCurr = strCurr.substring(strCurr.indexOf("\nCurrent value: "));
		            strCurr = strCurr.replace(oldCurr, "");
	            }
	            pref.setSummary(strCurr + "\nCurrent value: " + editPref.getText().toString());
	        }
	        
	        if (pref instanceof ListPreference) 
	        {
	        	ListPreference listPref = (ListPreference) pref;
	            String strCurr = pref.getSummary().toString();
	            if (strCurr.indexOf("\nCurrent value: ") >= 0)
	            {
	            	String oldCurr = strCurr.substring(strCurr.indexOf("\nCurrent value: "));
		            strCurr = strCurr.replace(oldCurr, "");
	            }
	            pref.setSummary(strCurr + "\nCurrent value: " + listPref.getEntry());
	        }
	    }
	
	    // Set up a listener whenever a key changes            
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() 
	{
	    super.onPause();
	
	    // Unregister the listener whenever a key changes            
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
	    Preference pref = findPreference(key);
	
	    if (pref instanceof EditTextPreference) {
	    	EditTextPreference editPref = (EditTextPreference) pref;
	        String strCurr = pref.getSummary().toString(); 
            if (strCurr.indexOf("\nCurrent value: ") >= 0)
            {
            	String oldCurr = strCurr.substring(strCurr.indexOf("\nCurrent value: "));
	            strCurr = strCurr.replace(oldCurr, "");
            }
	        pref.setSummary(strCurr + "\nCurrent value: " + editPref.getText().toString());
	        //pref.setSummary(listPref.getText().toString());
	    }
	    
	    if (pref instanceof ListPreference) {
	    	ListPreference listPref = (ListPreference) pref;
	        String strCurr = pref.getSummary().toString(); 
            if (strCurr.indexOf("\nCurrent value: ") >= 0)
            {
            	String oldCurr = strCurr.substring(strCurr.indexOf("\nCurrent value: "));
	            strCurr = strCurr.replace(oldCurr, "");
            }
	        pref.setSummary(strCurr + "\nCurrent value: " + listPref.getEntry());
	        //pref.setSummary(listPref.getText().toString());
	    }
	}
	
}
