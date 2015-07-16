/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener 
{
	private boolean own_procces = true;
	private final GestureListener listener = new GestureListener();
    private final GestureDetector gestureDetector = new GestureDetector(listener);

    // We can be in one of these 2 states
    static final int NONE = 0;
    static final int ZOOM = 1;
    static final int MOVE = 2;
    int mode = NONE;

    static final int MIN_FONT_SIZE = 10;
    static final int MAX_FONT_SIZE = 50;

    float oldDist = 1f;
    float scale = 1f;
    float prevDist = 0f;
    
    public OnSwipeTouchListener()
    {
    	this.own_procces = true;
    }

    public OnSwipeTouchListener(boolean own_procces)
    {
    	this.own_procces = own_procces;
    }
    
    public boolean onTouch(final View view, final MotionEvent motionEvent) 
    {
     	touch(motionEvent);
    	checkPinch(view, motionEvent);
    	checkTouchMove(view, motionEvent);
    	
    	if (mode == ZOOM)
    		return true;
    	
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) 
        {
            case MotionEvent.ACTION_UP:
            {
            	listener.lastScrollX = 0.0f;
            	listener.lastScrollY = 0.0f;
            	listener.firstY = true;
            }
        }
    	
        if (own_procces)
        	return gestureDetector.onTouchEvent(motionEvent);
        
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    private float spacing(MotionEvent event) 
    {
    	if (event == null || (event.getPointerCount() < 2))
    		return 0.0f;
    		
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
    public boolean checkPinch(View v, MotionEvent event) 
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK) 
        {
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) 
                {
                	mode = ZOOM;
                	Log.d(TAG, "mode=ZOOM" );
                	prevDist = oldDist;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == ZOOM) 
                {
                	if (scale > 1) 
                		pinchOut();
                    else 
                        pinchIn();
                }
                mode = NONE;
                prevDist = 0f;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) 
                {
                	float newDist = spacing(event);
                    if (newDist > 10f) 
                    {
                        scale = newDist / oldDist;

                        if (scale > 1) 
                            scale = 1.1f;
                        else 
                        	if (scale < 1) 
                                scale = 0.95f;
                        
                    }
                    
                   	pinchMove(prevDist < newDist);
                   	prevDist = newDist;
                }
                break;
        }
        return false;
    }
    
    public boolean checkTouchMove(View v, MotionEvent e) 
    {
    	if (mode != NONE && mode != MOVE)
    		return false;
    	
        float x = e.getX();
        float y = e.getY();
    	switch (e.getAction() & MotionEvent.ACTION_MASK) 
        {
            case MotionEvent.ACTION_DOWN:
            	mode = MOVE;
            	touchDown((int)x, (int)y);
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d(TAG, "Touch Move: (" + x + "," + y + ")");
                touchMove((int)x, (int)y);
                break;
        }
        return false;
    }
    
    private final class GestureListener extends SimpleOnGestureListener 
    {

        private static final float SWIPE_THRESHOLD = 50.0f;
        private static final float SWIPE_VELOCITY_THRESHOLD = 100.0f;

        private static final float SCROLL_THRESHOLD = 10.0f;
        public float lastScrollX = 0.0f;
        public float lastScrollY = 0.0f;
        public boolean firstY = true;
        
        @Override
        public boolean onDown(MotionEvent e) 
        {
        	lastScrollX = 0.0f;
        	lastScrollY = 0.0f;
        	firstY = true;
        	
        	Log.v(TAG, "=OnSwipe onDown event="+e);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) 
        {
            float x = e.getX();
            float y = e.getY();

            Log.d(TAG, "Double Tap: Tapped at: (" + x + "," + y + ")");
            doubleTap((int)x, (int)y);
            return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
        {
            //Log.d("onScroll", "scroll at: (" + ((e2.getY() - lastScrollY)) + "," + SCROLL_THRESHOLD + ")");
            if (lastScrollX != 0.0f && lastScrollY != 0.0f)
            {
            	if (firstY)
            	{
            		firstY = false;
            		return true;
            	}	
            	
            	float deltaX = (e2.getX() - lastScrollX);
            	float deltaY = (e2.getY() - lastScrollY);
            	
            	if (Math.abs(deltaX) < Math.abs(deltaY))
            	{	
                	if (deltaY > SCROLL_THRESHOLD)
	                	scrollDown();
	                else
	                    if (deltaY < (-SCROLL_THRESHOLD))
	                    	scrollUp();
	                    else
	                    	return true;
            	}
            }
            
            lastScrollX = e2.getX();
            lastScrollY = e2.getY();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) 
        {
            float x = e.getX();
            float y = e.getY();

            Log.d(TAG, "=OnSwipe Long press: Tapped at: (" + x + "," + y + ")");
            longPress((int)x, (int)y);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) 
        {
            float x = e.getX();
            float y = e.getY();

        	Log.v(TAG, "=OnSwipe onSingleTapConfirmed event="+e);

            singleTap((int)x, (int)y);
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
        {
        	Log.v(TAG, "=OnSwipe onFling event1="+e1+" event2="+e2);
        	
            boolean result = true;
            try 
            {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) 
                {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) 
                    {
                        if (diffX > 0.0f) 
                        {
                            //Log.d("Single Tap", "swipeRight!");
                            swipeRight((int)e2.getX(), (int)e2.getY());
                        } 
                        else 
                        {
                            //Log.d("Single Tap", "swipeLeft!");
                            swipeLeft((int)e2.getX(), (int)e2.getY());
                        }
                        return result;
                    }
                } 
                else 
                {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) 
                    {
                        if (diffY > 0) 
                        {
                            swipeBottom();
                        } 
                        else 
                        {
                            swipeTop();
                        }
                        return result;
                    }
                }
                onSingleTapConfirmed(e1);
            } 
            catch (Exception exception) 
            {
            //    exception.printStackTrace();
            }
            return result;
        }
    }

    public void touch(MotionEvent event){};
    public void touchDown(int x, int y){};
    public void touchMove(int x, int y){};
    public void swipeLeft(int x, int y){};
    public void swipeRight(int x, int y){};
    public void swipeTop(){};
    public void swipeBottom(){};
    public void doubleTap(int x, int y){};
    public void singleTap(int x, int y){};
    public void longPress(int x, int y){};
    public void pinchOut(){};
    public void pinchIn(){};
    public void pinchMove(boolean isGrow){};
    public void scrollUp(){};
    public void scrollDown(){};
    
	final public static String TAG = "OnSwipeTouchListener";
}	
