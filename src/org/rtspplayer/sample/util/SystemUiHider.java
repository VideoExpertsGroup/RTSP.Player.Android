/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;

public abstract class SystemUiHider 
{
	public static final int FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES = 0x1;
	public static final int FLAG_FULLSCREEN = 0x2;
	public static final int FLAG_HIDE_NAVIGATION = FLAG_FULLSCREEN | 0x4;

	protected Activity mActivity;

	public View mAnchorView;

	protected int mFlags;
	protected boolean locked = false;

	protected OnVisibilityChangeListener mOnVisibilityChangeListener = sDummyListener;

	public static SystemUiHider getInstance(Activity activity, View anchorView, int flags) 
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			return new SystemUiHiderHoneycomb(activity, anchorView, flags);
		} 
		else 
		{
			return new SystemUiHiderBase(activity, anchorView, flags);
		}
	}

	protected SystemUiHider(Activity activity, View anchorView, int flags) 
	{
		mActivity = activity;
		mAnchorView = anchorView;
		mFlags = flags;
	}

	public abstract void setup();
	public abstract boolean isVisible();
	public abstract void hide();
	public abstract void hideLocked();
	public abstract void show();
	public abstract void hideResize();
	public abstract void showWithResize();
	public abstract void resize(); 

	public void lock(final boolean locked) 
	{
		this.locked = locked;
	}
	
	public void toggle() 
	{
		if (isVisible()) 
		{
			hide();
		} 
		else 
		{
			show();
		}
	}

	public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) 
	{
		if (listener == null) 
		{
			listener = sDummyListener;
		}

		mOnVisibilityChangeListener = listener;
	}

	private static OnVisibilityChangeListener sDummyListener = new OnVisibilityChangeListener() 
	{
		@Override
		public void onVisibilityChange(boolean visible) 
		{
		}
	};

	public interface OnVisibilityChangeListener 
	{
		public void onVisibilityChange(boolean visible);
	}
}
