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
import android.util.AttributeSet;
import android.util.Log;

public class IntEditTextPreference extends EditTextPreference 
{

    public IntEditTextPreference(Context context) 
    {
        super(context);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
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