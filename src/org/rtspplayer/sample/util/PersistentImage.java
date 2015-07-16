/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.rtspplayer.sample.R;

import android.content.Context;
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
import android.util.Log;

public class PersistentImage
{
	private Context context = null;
	private File 	storage = null;
	
	public PersistentImage(Context context, String directory_name) 
	{
		this.context = context;
		try 
		{
			this.storage = new File(context.getExternalFilesDir(null), directory_name);
		    if (this.storage != null && !this.storage.exists())
		    {
		    	if (!this.storage.mkdirs())
		    		this.storage = null;
		    }
		}
		catch (Exception e) {}
	}
	
	public String getJpegFileName()
	{
		if (storage == null)
			return "";
		
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
	    return (storage.getPath() + File.separator + timeStamp + ".jpg");
	}
	
	public boolean saveJpeg(String saveToFile, Bitmap bitmap)
	{
		FileOutputStream fos = null;
		try 
		{
			fos = new FileOutputStream(saveToFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			return true;
		} 
		catch (FileNotFoundException e) {} 
		catch (IOException e) {}
		
		return false;
	}
	
	boolean saveJpeg(String saveToFile, ByteBuffer frame, int width, int height)
	{
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		frame.rewind();
		bitmap.copyPixelsFromBuffer(frame);
		
		FileOutputStream fos = null;
		try 
		{
			fos = new FileOutputStream(saveToFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			return true;
		} 
		catch (FileNotFoundException e) {} 
		catch (IOException e) {}
		
		return false;
	}

	final public static String TAG = "PersistentImage";
}	
