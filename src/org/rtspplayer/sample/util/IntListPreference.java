/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class IntListPreference extends ListPreference
{
    public IntListPreference(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }

    public IntListPreference(Context context) 
    {
        super(context);
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

    @Override
    protected String getPersistedString(String defaultReturnValue) 
    {
        if(getSharedPreferences().contains(getKey())) 
        {
            int intValue = getPersistedInt(0);
            return String.valueOf(intValue);
        } 
        else 
        {
            return defaultReturnValue;
        }
    }
}