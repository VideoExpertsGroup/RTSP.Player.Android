/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */

package org.rtspplayer.sample.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;





import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import veg.mediaplayer.sdk.Thumbnailer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.Thumbnailer.ThumbnailFrame;
import veg.mediaplayer.sdk.Thumbnailer.ThumbnailerState;

import org.rtspplayer.sample.R;
import org.rtspplayer.sample.activity.MainActivity;
import org.rtspplayer.sample.adapter.CamerasList.ExtractLinkThumbs;
import org.rtspplayer.sample.adapter.GridAdapter.GridAdapterCallback;
import org.rtspplayer.sample.data.GridData;
import org.rtspplayer.sample.data.GridData.ContentType;
import org.rtspplayer.sample.data.GridData.GridDataComparator;
import org.rtspplayer.sample.data.GridData.UpdateState;
import org.rtspplayer.sample.data.StreamItem;
import org.rtspplayer.sample.database.ChannelListTable;
import org.rtspplayer.sample.database.DatabaseHelper;
import org.rtspplayer.sample.util.PersistentImage;
import org.rtspplayer.sample.util.ShadowImage;
import org.rtspplayer.sample.util.SharedSettings;
import org.rtspplayer.sample.util.StorageDrives;
import org.rtspplayer.sample.util.SupportedFormats;
import org.rtspplayer.sample.util.ThreadPool;
import org.rtspplayer.sample.util.StorageDrives.StorageInfo;


public class StreamsList extends GridAdapter 
{
    private Context context;
    private DatabaseHelper db;
    
    private ShadowImage drawableBlank = null;
    private float shadowRadius = 1f;
	private PointF shadowDirection = new PointF(1f, 1f);
    private float thumbWidth = 96f;
    private float thumbHeight = 72f;
    
    private ExtractLinkThumbs linkExtractor = null;
    
    private boolean is_image_persistent = true;
    
    public StreamsList(Context c, final DatabaseHelper db, GridAdapterCallback callback) 
    {
		super(c, callback);
		this.context = c;
        this.db = db;
        
        thumbWidth = pxFromDp(thumbWidth);
        thumbHeight = pxFromDp(thumbHeight);
        shadowDirection = new PointF(pxFromDp(shadowDirection.x), pxFromDp(shadowDirection.y));
        shadowRadius = pxFromDp(shadowRadius);
        drawableBlank = new ShadowImage(mContext.getResources(), BitmapFactory.decodeResource(c.getResources(), R.drawable.blank_camera2), 
        						thumbWidth, thumbHeight, shadowDirection, shadowRadius);
    }
    	
   	public boolean Draw(final Context context, final View view, final ContentType type, float thumbWidth, float thumbHeight, float shadowRadius, PointF shadowDirection)
   	{
   		ImageView imbThumb = (ImageView) view.findViewById(R.id.document_item);
   		imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
       
		imbThumb.setImageDrawable(drawableBlank);
		imbThumb.setScaleType(ScaleType.FIT_CENTER);
       
   		return true;
   	}
   
	@Override
	public boolean Load() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Refresh() 
	{
		// try refresh list
		synchronized (itemList) 
		{
		    Close();
			ArrayList<GridData> checkedItems=new ArrayList<GridData>();
			String curItem=null;
			for(int i=1;i<5;++i) {
				curItem = PreferenceManager.getDefaultSharedPreferences(mContext).getString("stream" +i, null);


				checkedItems.add(new GridData(curItem, null, null, null, 0, 0, null));
			}

			itemList.clear();
			checkedCams.clear();
		    Cursor c = db.readChannelForPresentation(ITEM_TAG);
			while (c.moveToNext())
		    {
				GridData cam=new StreamItem(c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_NAME)),
						c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_URL)),
						c.getString(c.getColumnIndexOrThrow(ChannelListTable.USER)),
						c.getString(c.getColumnIndexOrThrow(ChannelListTable.PASSWORD)),
						c.getInt(c.getColumnIndexOrThrow(ChannelListTable._ID /*CHANNEL_ID*/)),
						c.getInt(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_IMAGE_URL)),
						c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_IMAGE_URL_STR)));
				if(checkedItems.contains(cam)) {
					cam.chosen = true;
					cam.checkBoxIndex=1+checkedItems.indexOf(cam);
					checkedCams.add(cam);
					setSlotBusy(cam.checkBoxIndex);
				}
		    	itemList.add(cam);
		    }
		}
		
	    notifyDataSetChanged();
	    
	    int thumbnailThreadCount = SharedSettings.getInstance().thumbnailThreadCount;
	    if (thumbnailThreadCount <= 0)
	    	return true;
	    
	    if (thumbnailThreadCount > 20)
	    	thumbnailThreadCount = 20;
	    
	    linkExtractor = new ExtractLinkThumbs();
	    
	    linkExtractor.start(thumbnailThreadCount);
	    
		return true;
	}

	@Override
	public boolean Close() 
	{
	    if (linkExtractor != null)
	    {
	    	linkExtractor.stop();
	    	linkExtractor = null;
	    }
	    
		return false;
	}

	@Override
	public boolean StartThumbnailUpdate() 
	{
		StopThumbnailUpdate();
		
	    int thumbnailThreadCount = SharedSettings.getInstance().thumbnailThreadCount;
	    if (thumbnailThreadCount <= 0)
	    	return true;
	    
	    if (thumbnailThreadCount > 20)
	    	thumbnailThreadCount = 20;
	    
	    linkExtractor = new ExtractLinkThumbs();
	    linkExtractor.start(thumbnailThreadCount);
		return true;
	}
	
	@Override
	public boolean StopThumbnailUpdate() 
	{
	    if (linkExtractor != null)
	    {
	    	linkExtractor.stop();
	    	linkExtractor = null;
	    }
	    
		return true;
	}

	@Override
	public GridData getFirstItem() 
	{
		GridData selGd = null;
		for( int i = 0; i < getCount(); i++)
		{
			GridData gd = (GridData)getItem(i);
			if (!gd.isDirectory)
			{
				selGd = gd;
				break;
			}
		}
		
	    return selGd;
	}

	@Override
	public GridData getSelectedItem() 
	{
		return get_sel();
	}

	@Override
	public GridData getNextSelectedItem(long id) 
	{
		boolean bFound = false;
		
		GridData selGd = null;

		ArrayList<GridData> searchableArr=null;

		if(MainActivity.screenMode!= MainActivity.ScreenMode.MultiView)
			searchableArr=itemList;
		else
		    searchableArr=MainActivity._2x2camerasData;

    	for( GridData gd : searchableArr)
    	{
			if (bFound)
			{
				selGd = gd;
				break;
			}

    		if(gd.id == id)
    			bFound = true;
    	}

        if (!bFound || selGd == null)
        	return null;
        
        return selGd;
	}

	@Override
	public GridData getPreviousSelectedItem(long id) 
	{
		boolean bFound = false;
		
		GridData selGd = null;
		ArrayList<GridData> searchableArr=null;

		if(MainActivity.screenMode!= MainActivity.ScreenMode.MultiView)
			searchableArr=itemList;
		else
			searchableArr=MainActivity._2x2camerasData;

		for( GridData gd : searchableArr)
    	{
    		if(gd.id == id)
    			break;
    		
			bFound = true;
			selGd = gd;
    	}
    	
        if (!bFound || selGd == null)
        	return null;

        return selGd;
	}

	@Override
	public boolean SelectItem(long id) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean SelectItem(GridData item) 
	{
		set_sel(item);
		notifyDataSetInvalidated();
		return true;
	}

	@Override
	public boolean GoBackFromSelectedItem() 
	{
		return false;
	}

	@Override
	public boolean isItemExistByUrl(String url) 
	{
		for( GridData gd : itemList)
			if(gd.url.equals(url))
				return true;
		
		return false;
	}

	@Override
	public GridData getItem(long id) 
	{
    	for(GridData item : itemList)
    		if(item.id == id)
    			return item; 

    	return null;
	}

	@Override
	public boolean AddItem(String name, String url, String user,
								String password, int channel_id, int preview, String image_file) 
	{
		long id = db.addChannel(name, url, ITEM_TAG, user, password, channel_id, preview, image_file);
		if (id != -1)
		{
			itemList.add(new StreamItem(name, url, user, password, id, preview, image_file));
			notifyDataSetChanged();
		}
		return true;
	}

	@Override
	public boolean UpdateItem(long id, String name, String url, String user,
								String password, int channel_id, int preview, String image_file) 
	{
    	GridData gdf = getItem(id);
    	if(gdf == null)
    		return false;
    	
		Log.v(TAG, "=>updateChannel " + image_file);
		
    	gdf.name = name;
    	gdf.url = url;
    	gdf.user = user;
    	gdf.password = password;
    	gdf.image = preview;
    	
    	if (image_file != null && !image_file.isEmpty())
    		gdf.image_file = image_file; 
    	
    	db.updateChannel(gdf);
    	notifyDataSetChanged();
    	return true;
	}

	@Override
	public boolean DeleteItem(long id) 
	{
		db.deleteChannel(id);
		for (GridData gridData : itemList)
			if (gridData != null && gridData.id == id) 
			{
				if(gridData.image_file != null && gridData.image_file.length() > 0)
			    {
				    File f = new File(gridData.image_file);
				    if(f.exists())
				    	f.delete();
			    }
			    
				itemList.remove(gridData);
				break;
			}
		notifyDataSetChanged();
		return true;
	}
	
    class ExtractLinkThumbs 
    {
    	private int nThreadCount = 0;
    	private ThreadPool threadPool = null;

    	private boolean finish = false;
    	
		private Handler  mainUiHandler = new Handler(mContext.getMainLooper());
		private Runnable runnableUIUpdate = new Runnable()
        {
            @Override
            public void run() 
            {
            	notifyDataSetChanged();
            }
        };
        
        public void start(final int nThreadCount)
        {
        	if (threadPool != null || nThreadCount <= 0)
        		return;
        	
        	this.finish = false;
        	this.nThreadCount = nThreadCount;
        	
        	if (threadPool == null)
        		threadPool = new ThreadPool(nThreadCount);
        	
            for (int i = 0; i < nThreadCount; i++) 
            {
                threadPool.runTask(createTask(i));
            }
        }
    
        public void stop()
        {
        	if (threadPool == null)
        		return;
        	
        	finish = true;
//			for (int i = 0; i < itemList.size(); i++)
//			{
//				GridData gd = itemList.get(i);
//				if (gd.thumbnailer != null)
//				{
//					gd.thumbnailer.Close();
//					//gd.thumbnailer = null;
//				}
//			}
			
        	//threadPool.join();
        	threadPool.close();
        	threadPool = null;
        }
        
        private Runnable createTask(final int taskID) 
        {
        	return new Runnable() 
            {
        		public void run() 
        		{
        			Log.v(TAG, "Task " + taskID + ": start");
	
        			int count = 0;
    				synchronized (itemList) 
    				{
    					count = itemList.size();
    				}
    				
	    			for (int i = 0; (i < count && !finish); i++)
	    			{
	    				GridData gd = null;
	    				synchronized (itemList) 
	    				{
	    					gd = itemList.get(i);
		    				if (finish) break;
		    				
		    				if (gd.update_state != UpdateState.none || gd.draw != null ||
		    						(is_image_persistent && gd.image_file != null && !gd.image_file.isEmpty()))
		    					
		    					continue;
		    				
		    				if (finish) break;
	
		    				gd.update_state = UpdateState.updating;
		    				gd.thumbnailer = new Thumbnailer(context); 
		        			Log.v(TAG, "Task " + taskID + ": url - " + gd.url);
	    				
		    				if (gd == null || gd.thumbnailer == null)
		    				{
		    					if (gd != null)
				    				gd.update_state = UpdateState.none;
		    					continue;
		    				}
	    				}
	    				
		                try 
		                {
		    				synchronized (itemList) 
		    				{
			    				if (finish)
			    				{
			    					gd.thumbnailer = null;
				    				gd.update_state = UpdateState.none;
				        			Log.v(TAG, "Task " + taskID + ": finish.");
			    					break;
			    				}
		    				}
		    				
		    				Object waiter = gd.thumbnailer.Open(gd.url);
		    	    		synchronized (waiter) 
		    	    		{
		    	    		    try 
		    	    		    {
		    	    		    	waiter.wait();
		    	    		    }
		    	    		    catch (InterruptedException e) {}
		    	    		}
			                	
		    				if (gd == null || gd.thumbnailer == null)
		    				{
		    					if (gd != null)
					                synchronized (itemList)
					                {
					                	gd.update_state = UpdateState.none;
					                }
		    					continue;
		    				}
		    				
			                synchronized (itemList)
			                {
			    				if (gd.thumbnailer.getState() != ThumbnailerState.Opened)
			    				{
				    				gd.thumbnailer.Close();
				    				gd.thumbnailer = null;
				    				gd.update_state = UpdateState.none;
			    					continue;
			    				}
			                }
			                
		    				ThumbnailFrame shot = gd.thumbnailer.getFrame();
		        			
		    				gd.thumbnailer.Close();
		    				gd.thumbnailer = null;

		    				if (finish)
		    				{
			        			Log.v(TAG, "Task " + taskID + ": finish.");
		    					break;
		    				}
		        			
		    				if(shot == null || shot.getData() == null)
		    				{
			        			Log.v(TAG, "Task " + taskID + ": empty shot.");
				                synchronized (itemList)
				                {
				                	gd.update_state = UpdateState.update;
				                }
		    					continue;
		    				}
		    						
		        			Log.v(TAG, "Task " + taskID + ": get video shot - " + shot.getData());
		    				if (finish)
		    				{
			        			Log.v(TAG, "Task " + taskID + ": finish.");
		    					break;
		    				}

		    				shot.getData().rewind();
		    				Bitmap bmp = Bitmap.createBitmap(shot.getWidth(), shot.getHeight(), Bitmap.Config.ARGB_8888);
		    				bmp.copyPixelsFromBuffer(shot.getData());

							shot = null;
		    				
		    				if (finish)
		    				{
			        			Log.v(TAG, "Task " + taskID + ": finish.");
		    					break;
		    				}

		    				if(bmp != null)
		    				{
		    					gd.draw = new ShadowImage(mContext.getResources(), bmp, 
		    										pxFromDp(96), pxFromDp(72), new PointF(pxFromDp(3), pxFromDp(3)), pxFromDp(2f));
								gd.thumbnailer = null;								
		    					gd.isUpdate = true;
		    					
		    					if (is_image_persistent)
		    					{
				    				synchronized (itemList) 
				    				{
				    					PersistentImage persistent = new PersistentImage(mContext, mContext.getResources().getString(R.string.storage_thumbs));
				    					gd.image_file = persistent.getJpegFileName();
				    					if (gd.image_file != null && !gd.image_file.isEmpty())
				    					{
				    						persistent.saveJpeg(gd.image_file, bmp);
				    				    	db.updateChannel(gd); // save image
				    					}
				    				}
		    					}
		    					else
		    					{
				    				synchronized (itemList) 
				    				{
			    						gd.image_file = "";
			    				    	db.updateChannel2(gd); 
				    				}
		    					}
			    				
			    				mainUiHandler.post(runnableUIUpdate);
			        			Log.v(TAG, "Task " + taskID + ": bitmap - " + bmp);
		    				}

		    				if (finish)
		    				{
			        			Log.v(TAG, "Task " + taskID + ": finish.");
		    					break;
		    				}
		        			
		                	Thread.sleep(1);
		                } 
		                catch (InterruptedException ex) 
		                { 
			                synchronized (itemList)
			                {
			                	gd.update_state = UpdateState.none;
			                }
			                
		        			Log.v(TAG, "Task " + taskID + ": interrupted.");
		                	break; 
		                }
		                
		                synchronized (itemList)
		                {
		                	gd.update_state = UpdateState.update;
		                }
	    			}
	    			
        			Log.v(TAG, "Task " + taskID + ": end");
        		}
            };
        }
    };
	
	final public static String TAG = "StreamsList";
	final public static String ITEM_TAG = "my";
}