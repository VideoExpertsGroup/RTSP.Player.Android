/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

public class SystemUiHiderBase extends SystemUiHider 
{
	private boolean mVisible = true;

	protected SystemUiHiderBase(Activity activity, View anchorView, int flags) 
	{
		super(activity, anchorView, flags);
	}

	@Override
	public void setup() 
	{
		if ((mFlags & FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES) == 0) 
		{
			mActivity.getWindow().setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
											WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	@Override
	public boolean isVisible() 
	{
		return mVisible;
	}

	@Override
	public void hide() 
	{
		if ((mFlags & FLAG_FULLSCREEN) != 0) 
		{
			mActivity.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mOnVisibilityChangeListener.onVisibilityChange(false);
		mVisible = false;
	}

	@Override
	public void show() 
	{
		if ((mFlags & FLAG_FULLSCREEN) != 0) 
		{
			mActivity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mOnVisibilityChangeListener.onVisibilityChange(true);
		mVisible = true;
	}

	@Override
	public void hideResize() 
	{
		if ((mFlags & FLAG_FULLSCREEN) != 0) 
		{
			mActivity.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mOnVisibilityChangeListener.onVisibilityChange(false);
		mVisible = false;
	}

	@Override
	public void hideLocked() 
	{
	}
	
	@Override
	public void showWithResize() 
	{
		if ((mFlags & FLAG_FULLSCREEN) != 0) 
		{
			mActivity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mOnVisibilityChangeListener.onVisibilityChange(true);
		mVisible = true;
	}
	
	@Override
	public void resize() 
	{
	}
}
