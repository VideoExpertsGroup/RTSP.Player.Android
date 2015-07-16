/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

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

public class ShadowImage extends BitmapDrawable 
{
 	private final BitmapShader bitmapShader;
	private final Paint p;
	private final RectF rectRound;
	private final float borderRadius;    
	private final Paint mShadow;
	
    private float shadowRadius = 4f;
	private PointF shadowDirection = new PointF(3f, 3f);
    	
   	@Override
	public void draw(Canvas canvas) 
   	{
	    Rect rect = new Rect(0, 0, getBitmap().getWidth(), getBitmap().getHeight());
	    Log.i(TAG, rect.toString());
	    setBounds(rect);
	
	    canvas.drawRoundRect(rectRound, borderRadius, borderRadius, mShadow);
	    canvas.drawRoundRect(rectRound, borderRadius, borderRadius, p);
	}

	public ShadowImage(Resources res, Bitmap bitmap, float w, float h, PointF shadowDirection, float shadowRadius) 
	{
	    super(res, Bitmap.createScaledBitmap(bitmap, (int) (w + shadowDirection.x), (int) (h + shadowDirection.y), false));
	    
	    this.shadowRadius = shadowRadius;
	    this.shadowDirection = shadowDirection;
	    
	    final Bitmap bmp = getBitmap();
	    bitmapShader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
	    p = getPaint();
	    p.setAntiAlias(true);
	    p.setShader(bitmapShader);
	    final int wsrc = bmp.getWidth();
	    final int hsrc = bmp.getHeight();
	    rectRound = new RectF(0, 0, wsrc, hsrc);
	    borderRadius = shadowRadius * 0.02f * Math.min(wsrc, hsrc);
	    
	    mShadow = new Paint();
	    mShadow.setAntiAlias(true);
	    mShadow.setShadowLayer(shadowRadius, shadowDirection.x, shadowDirection.y, Color.DKGRAY);
	}
	
	final public static String TAG = "ShadowImage";
}	
