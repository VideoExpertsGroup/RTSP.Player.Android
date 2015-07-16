/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.rtspplayer.sample.activity.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent) 
	{
	    Intent myIntent = new Intent(context, MainActivity.class);
	    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(myIntent);
		
	}

}