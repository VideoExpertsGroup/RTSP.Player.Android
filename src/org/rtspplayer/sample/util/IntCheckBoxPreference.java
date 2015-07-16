/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;

public class IntCheckBoxPreference extends CheckBoxPreference 
{
	public static final String TAG = "IntCheckBoxPreference";
	
    public IntCheckBoxPreference(Context context) 
    {
        super(context);
    }

    public IntCheckBoxPreference(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }

    public IntCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) 
    {
    	int defaultValueInt = (defaultReturnValue ? 1 : 0);
    	
    	if (!shouldPersist()) {
    	     return defaultReturnValue;
    	}
    	
        int returnValue = getSharedPreferences().getInt(getKey(), defaultValueInt);
        Log.v(TAG, "getPersistedBoolean: " + getKey() + returnValue);
        
    	return (getSharedPreferences().getInt(getKey(), defaultValueInt) == 1);
    }
 
    @Override
    protected boolean persistBoolean(boolean value) 
    {
    	int valueInt = (value ? 1 : 0);
    	SharedPreferences.Editor editor = getSharedPreferences().edit();    	
    	editor.putInt(getKey(), valueInt);
    	return editor.commit();
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) 
    {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) 
    {
        if(value == null) 
            return false;

        try
    	{
        	int valueInt = Integer.valueOf(value);
            return persistInt(valueInt);
    	}
    	catch(NumberFormatException nfe)
    	{
    	}

        return false;
    }
}