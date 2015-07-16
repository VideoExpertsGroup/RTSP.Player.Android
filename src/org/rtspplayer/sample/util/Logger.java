/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.TimeZone;

import org.rtspplayer.sample.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

public class Logger
{
	private Context context = null;
	public Logger(Context context) 
	{
		this.context = context;
	}
	
	public String getLogcat()
	{
		try 
		{  
		    ArrayList<String> commandLine = new ArrayList<String>();  
		    commandLine.add( "logcat");  
		    commandLine.add( "-d");  
		    commandLine.add( "-v");  
		    commandLine.add( "time");  
		    commandLine.add( "*.*");  
		    Process process = Runtime.getRuntime().exec( commandLine.toArray( new String[commandLine.size()]));  
		    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()));
		    StringBuilder log = new StringBuilder();
		    String line = "";
		    while ((line = bufferedReader.readLine()) != null) 
		    {
		    	log.append(line);
		    	log.append("\n");
		    }
		    
		    return log.toString();
	    } 
		catch ( IOException e) 
		{  
		}	
		
		return "";
	}

	public boolean getLogcat(final File file)
	{
		try 
		{  
		    ArrayList<String> commandLine = new ArrayList<String>();  
		    commandLine.add( "logcat");  
		    commandLine.add( "-d");  
		    commandLine.add( "-v");  
		    commandLine.add( "threadtime");  
		    commandLine.add( "*.*");  
		    Process process = Runtime.getRuntime().exec( commandLine.toArray( new String[commandLine.size()]));  
		    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()));

	        FileOutputStream fos = new FileOutputStream(file);
	        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		    PrintWriter pw = new PrintWriter(osw);

		    String line = "";
		    while ((line = bufferedReader.readLine()) != null) 
		    {
		        pw.println(line);
		    }

		    pw.flush();
	        pw.close();
		    return true;
	    } 
		catch ( IOException e) 
		{  
		}	
		
		return false;
	}
	
	public boolean sendLogByEmail(final String email, String subj, String text )
	{
		if (email == null || email.isEmpty() || subj == null || text == null )
			return false;
		
		if (subj.isEmpty())
		{
			subj = "RTSP Player log: ";
		}
		
		if (text.isEmpty())
		{
			text = "logcat -d -v threadtime *.* from RTSP Player.";
		}
		
        File temp = null;
		try 
		{
			temp = File.createTempFile("log_current",".log", context.getCacheDir());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}	   
        
		if (temp == null)
			return false;
		
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("*/*");
        
        temp.setReadable(true, false);
        if (!getLogcat(temp))
        	return false;
        
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		String date = fmt.format(new Date());
		
        String zipName = context.getCacheDir() + "/log.zip";
        Compress c = new Compress(new String[] {temp.getAbsolutePath()}, zipName);
        c.zip();	        
        		
        File send = new File(zipName);
        send.setReadable(true, false);
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(send));
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        i.putExtra(Intent.EXTRA_SUBJECT, subj + date);
        i.putExtra(Intent.EXTRA_TEXT, text);

        context.startActivity(createEmailOnlyChooserIntent(i, "Send via email"));
        return true;
	}
	
	private Intent createEmailOnlyChooserIntent(Intent source, CharSequence chooserTitle) 
	{
		Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@domain.com", null));
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) 
        {
        	Intent target = new Intent(source);
        	target.setPackage(ri.activityInfo.packageName);
        	intents.add(target);
        }

        if(!intents.isEmpty()) 
        {
        	Intent chooserIntent = Intent.createChooser(intents.remove(0), chooserTitle);
        	chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
        	return chooserIntent;
        } 
        else 
        {
        	return Intent.createChooser(source, chooserTitle);
        }
	}

	final public static String TAG = "Logger";
}	
