/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.rtspplayer.sample.R;
import org.rtspplayer.sample.activity.MainActivity;
import org.rtspplayer.sample.adapter.CamerasList.ExtractLinkThumbs;
import org.rtspplayer.sample.adapter.GridAdapter.GridAdapterCallback;
import org.rtspplayer.sample.data.FileItem;
import org.rtspplayer.sample.data.GridData;
import org.rtspplayer.sample.data.GridData.ContentType;
import org.rtspplayer.sample.data.GridData.GridDataComparator;
import org.rtspplayer.sample.data.GridData.UpdateState;
import org.rtspplayer.sample.database.DatabaseHelper;
import org.rtspplayer.sample.util.ShadowImage;
import org.rtspplayer.sample.util.SharedSettings;
import org.rtspplayer.sample.util.StorageDrives;
import org.rtspplayer.sample.util.SupportedFormats;
import org.rtspplayer.sample.util.ThreadHelper;
import org.rtspplayer.sample.util.StorageDrives.StorageInfo;

import veg.mediaplayer.sdk.Thumbnailer;
import veg.mediaplayer.sdk.Thumbnailer.ThumbnailFrame;
import veg.mediaplayer.sdk.Thumbnailer.ThumbnailerState;

public class FilesList extends GridAdapter 
{
	public final static boolean USE_THUMB_ANDROID = false;
    private Context context;
    
    private File mDir = null; //base dir
    private File mRootDir = null; //base dir
    private ExtractFileThumbs mThreadThumbs = null;
    
    private ShadowImage drawableAudioBlank = null;
    private ShadowImage drawableVideoBlank = null;

    private float shadowRadius = 1f;
	private PointF shadowDirection = new PointF(1f, 1f);
    private float thumbWidth = 96f;
    private float thumbHeight = 72f;
    
    private StorageDrives stor_drives = null;
    public FilesList(final Context c, final DatabaseHelper db, GridAdapterCallback callback) 
	{
		super(c, callback);
		context = c;
		stor_drives = new StorageDrives(c);
		
		mDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if(mDir != null){
			mDir = mDir.getParentFile();
		}
        stor_drives.update_stor_space();
        mRootDir = mDir;
        
        thumbWidth = pxFromDp(thumbWidth);
        thumbHeight = pxFromDp(thumbHeight);
        shadowDirection = new PointF(pxFromDp(shadowDirection.x), pxFromDp(shadowDirection.y));
        shadowRadius = pxFromDp(shadowRadius);
        
        drawableAudioBlank = new ShadowImage(mContext.getResources(), BitmapFactory.decodeResource(c.getResources(), R.drawable.blank_camera2_audio), 
        						thumbWidth, thumbHeight, shadowDirection, shadowRadius);

        drawableVideoBlank = new ShadowImage(mContext.getResources(), BitmapFactory.decodeResource(c.getResources(), R.drawable.blank_camera2), 
				thumbWidth, thumbHeight, shadowDirection, shadowRadius);
	}
	
	public boolean Draw(final Context context, final View view, final ContentType type, float thumbWidth, float thumbHeight, float shadowRadius, PointF shadowDirection)
	{
		ImageView imbThumb = (ImageView) view.findViewById(R.id.document_item);
		imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
   
		if (type == ContentType.Audio)
			imbThumb.setImageDrawable(drawableAudioBlank);
		else
			imbThumb.setImageDrawable(drawableVideoBlank);
			
		imbThumb.setScaleType(ScaleType.FIT_CENTER);
	   
		return true;
	}

    public boolean isRootDir(final File dir) 
    {
    	if(dir == null) 
    		return false;
    	
    	boolean ret = (mRootDir.compareTo(dir) == 0);
    	if(ret) 
    		return ret;
    	
    	List<StorageInfo> list_drives = stor_drives.get_storInfo();
    	if(list_drives.size() < 2)
    		return ret;
    	
    	int i = 0;
    	for(StorageInfo si : list_drives)
    	{
    		i++;
    		if(i == 1)
    			continue;
    		
    		String path = si.path;
    		File f = new File(path);
    		File fp = f.getParentFile();
    		
    		if(fp != null && fp.compareTo(dir)==0)
    			return true;
    		
    	}
    	
    	return false;
    }
    
//    public void update_list(final ArrayList<GridData> gridList)
//    {
//    	if (gridList == null)
//    		return;
//    	
//    	mGridList = gridList;
//    }
//    
    private boolean isFilesToPlayRecursive(File dir)
    {
    	if(dir == null || !dir.isDirectory() || dir.listFiles() == null)
    		return false;
    	
    	for(File f : dir.listFiles())
    	{
    		if(f == null)
    			return false;
    		
    		if(f.isDirectory())
    		{
        		if(isFilesToPlayRecursive(f))
        		{
        			return true;
        		}
        		continue;
    		}
    		
    		String sfile = f.getName();
    		if(sfile == null)
    			continue;
			
    		if(sfile.startsWith("."))
				continue;
			
			for(String s : SupportedFormats.VIDEO_SSFORMATS)
				if(sfile.endsWith(s))
					return true;

			for(String s : SupportedFormats.AUDIO_SSFORMATS)
				if(sfile.endsWith(s))
					return true;
    	}
    	
    	return false;
    }
    
    private boolean isFilesToPlayNonRecursive(File dir)
    {
    	if(dir == null || !dir.isDirectory())
    		return false;
    	
    	final FileFilter filter = new FileFilter() 
    	{
            @Override
            public boolean accept(File f) 
            {
            	if (f == null)
            		return false;
            	
            	if (f.isDirectory())
            		return true;
            			
        		String sfile = f.getName();
        		if(sfile == null)
        			return false;
    			
        		if(sfile.startsWith("."))
        			return false;
    			
    			for(String s : SupportedFormats.VIDEO_SSFORMATS)
    				if(sfile.endsWith(s))
    					return true;

    			for(String s : SupportedFormats.AUDIO_SSFORMATS)
    				if(sfile.endsWith(s))
    					return true;
    			
    			return false;
            }
        };

    	File[] listFiles = dir.listFiles(filter);
    	if (listFiles == null)
    		return false;
        
        List<File> queue = new LinkedList<File>();
        queue.addAll(Arrays.asList(listFiles));
        if (queue.isEmpty())
        	return false;

        for (ListIterator<File> itr = queue.listIterator(); itr.hasNext();) 
        {
            File file = itr.next();
            if (file == null)
            	continue;
            
            if (!file.isDirectory()) 
            	return true;
            
            itr.remove();
        	File[] sublistFiles = file.listFiles(filter);
        	if (sublistFiles == null)
        		continue;
        	
        	for (File f : sublistFiles) 
            	itr.add(f);
        }    	
    	
        return false;
    }

    public void set_root_dir()
    {
    	mDir = mRootDir;
    }
    
    public void update_dir(File dir)
    {
		Log.v(TAG, "=update_dir: " + mDir);
		checkedCams.clear();
    	if(dir != null)
    		mDir = dir;
    	
    	if(isRootDir(mDir))
    		mDir = mRootDir;
    	
		Log.v(TAG, "=update_dir: " + mDir);
		
		if(mDir == null)
			return;
        ArrayList<GridData> cashedFiles=new ArrayList<GridData>(itemList);
		itemList.clear();
		
		if(mThreadThumbs == null)
		{
			mThreadThumbs = new ExtractFileThumbs();
		}
		
		mThreadThumbs.stop(-1);
		
	    FilenameFilter filter = new FilenameFilter() 
	    {
			public boolean accept(File dir, String name) 
			{
				return true;
			}
		};

		Log.v(TAG, "=setState files ..." + mDir + "," + mRootDir + "," + !isRootDir(mDir) + "," + mDir.getParentFile());
		String[] str_files = mDir.list(filter);
		if(str_files == null)
			return;

		if(mDir.getParentFile() != null && !isRootDir(mDir))
		{
			Log.v(TAG, "=set Back" + mDir + "," + mRootDir + "," + !isRootDir(mDir));
			GridData gd = new GridData("..", mDir.getAbsolutePath(), "", "", 0, 0, "");
			gd.isBack = true;
			gd.isDirectory = true;
			gd.isUpdate = true;
			m_prev = gd;
		}
		
		
		for(String sfile : str_files)
		{
			File f = new File(mDir.getAbsolutePath(), sfile);
			//=>check ext
			if(sfile.startsWith("."))
				continue;
		
			if(!f.isDirectory())
			{
				boolean is_found= false;
				for(String s : SupportedFormats.VIDEO_SSFORMATS)
				{
					if(sfile.endsWith(s))
					{
						is_found = true;
						break;
					}
				}
				
				if(!is_found)
				{
					for(String s : SupportedFormats.AUDIO_SSFORMATS)
					{
						if(sfile.endsWith(s))
						{
							is_found = true;
							break;
						}
					}
					
					if(!is_found)
						continue;
				}
			}
			
			FileItem gd = new FileItem(removeExtension(sfile), sfile, "", "", 0, 0, "");
			if(f.isDirectory())
			{
				gd.isDirectory = true;
				gd.isUpdate = true;
				
				if(!isFilesToPlayRecursive(f))
					continue;
			}
			gd.url = f.getAbsolutePath();

			Log.v(TAG, " isdir=" + gd.isDirectory + " url=" + gd.url + "name=" + sfile);
//			if(cashedFiles.contains(gd))
//				gd.chosen=cashedFiles.get(cashedFiles.indexOf(gd)).chosen;
			itemList.add(gd);
		}
		Collections.sort(itemList, new GridData.GridDataComparator());
		
		//=>check sdcards
		if(isRootDir(mDir))
		{
			List<StorageInfo> list_drives = stor_drives.get_storInfo();
			if(list_drives != null && list_drives.size()>1)
			{
				int i=0;
				for(StorageInfo si : list_drives)
				{
					i++;
					if(i == 1)
						continue;

					String sfile = si.path;//+"/DCIM";
					File f = new File(sfile);
					if(f==null || !f.exists())
						continue;
					
					FileItem gd = new FileItem(si.getDisplayName(), sfile, "", "", 0, 0, "");

					if(f.isDirectory())
					{
						gd.isDirectory = true;
						gd.isUpdate = true;
						
						if(!isFilesToPlayRecursive(f))
							continue;
					}
					gd.url = f.getAbsolutePath();

					Log.v(TAG, " isdir=" + gd.isDirectory + " url=" + gd.url + "name=" + sfile);
//					if(cashedFiles.contains(gd))
//						gd.chosen=cashedFiles.get(cashedFiles.indexOf(gd)).chosen;
					itemList.add(gd);
				}
			}
		}
		//<=check sdcards
		Log.v(TAG, "<=setState files ...");
		
		mThreadThumbs.start();

    }
    
	private String removeExtension(String s) 
	{
	    String separator = System.getProperty("file.separator");
	    String filename;

	    // Remove the path upto the filename.
	    int lastSeparatorIndex = s.lastIndexOf(separator);
	    if (lastSeparatorIndex == -1) {
	        filename = s;
	    } else {
	        filename = s.substring(lastSeparatorIndex + 1);
	    }

	    // Remove the extension.
	    int extensionIndex = filename.lastIndexOf(".");
	    if (extensionIndex == -1)
	        return filename;

	    return filename.substring(0, extensionIndex);
	}

    class ExtractFileThumbs extends ThreadHelper
    {
		private Handler  mainUiHandler = new Handler(mContext.getMainLooper());
		private Runnable runnableUIUpdate = new Runnable()
        {
            @Override
            public void run() 
            {
            	notifyDataSetChanged();
            }
        };
		
		@Override
		public void runt() 
		{
			for (int i = 0; i < itemList.size(); i++)
			{
				GridData gd = itemList.get(i);
				
				if(!is_started())
					break;
 			
				if(gd.draw != null)
					continue;
				
				Bitmap bmp = null;
					//get Bitmap with using Android method
				if(USE_THUMB_ANDROID)
					bmp = ThumbnailUtils.createVideoThumbnail(gd.url, MediaStore.Images.Thumbnails.MINI_KIND);
    			if(bmp == null){
    				//get Bitmap with using Thumbnailer class
					Thumbnailer thumbnailer = new Thumbnailer(context);
    				Object waiter = thumbnailer.Open(gd.url);
    	    		synchronized (waiter) 
    	    		{
    	    		    try 
    	    		    {
    	    		    	waiter.wait();
    	    		    }
    	    		    catch (InterruptedException e) {}
    	    		}
    				if (thumbnailer.getState() != ThumbnailerState.Opened)
    				{
	    				thumbnailer.Close();
	    				thumbnailer = null;
    					continue;
    				}

    				String info = thumbnailer.getInfo();
        			Log.v(TAG, " get video info: " + info);
    				
    				ThumbnailFrame shot = thumbnailer.getFrame();
    				if(shot == null || shot.getData() == null)
    				{
    					continue;
    				}

        			Log.v(TAG, " get video shot - " + shot.getData());
        			
    				thumbnailer.Close();
    				thumbnailer = null;
    				
    				shot.getData().rewind();
    				bmp = Bitmap.createBitmap(shot.getWidth(), shot.getHeight(), Bitmap.Config.ARGB_8888);
    				bmp.copyPixelsFromBuffer(shot.getData());
    			}
				if(bmp != null)
				{
					gd.draw = new ShadowImage(mContext.getResources(), bmp, 
										pxFromDp(96), pxFromDp(72), new PointF(pxFromDp(3), pxFromDp(3)), pxFromDp(2f));
					gd.isUpdate = true;
				}

				mainUiHandler.post(runnableUIUpdate);
			}
		}
    	
		private float pxFromDp(float dp)
		{
		    return (dp * mContext.getResources().getDisplayMetrics().density);
		}    	
   };
    
	@Override
	public boolean Load() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Refresh() 
	{
		//balm. just for test
		/*
		Cursor cur = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		if(cur != null && cur.moveToFirst()){
			do{
				int column_index = cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				Uri filePathUri = Uri.parse(cur.getString(column_index));
				String fileName = filePathUri.getPath();
				Log.i(TAG, " =refresh file="+fileName);
			}while(cur.moveToNext());
		}*/
		
		//set_sel(null);
		update_dir(null);
        notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean Close() 
	{
		if(mThreadThumbs != null)
		{
			mThreadThumbs.stop(-1);
			mThreadThumbs = null;
		}
		
		return true;
	}

	@Override
	public boolean StartThumbnailUpdate() 
	{
		//Refresh();
		return true;
	}
	
	@Override
	public boolean StopThumbnailUpdate() 
	{
		//Close();
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
		GridData curGd = get_sel();
		if (curGd == null)
			return null;
		
		GridData selGd = null;
		boolean bFound = false;
		for( int i = 0; i < getCount(); i++)
		{
			GridData gd = (GridData)getItem(i);
			if (!gd.isDirectory && bFound)
			{
				selGd = gd;
				break;
			}
			
			if(!bFound && !gd.isDirectory && gd.url != null && gd.url.equalsIgnoreCase(curGd.url))
				bFound = true;
		}
		
	    if (!bFound || selGd == null)
	    	return null;
		
	    return selGd;
	}

	@Override
	public GridData getPreviousSelectedItem(long id) 
	{
		GridData curGd = get_sel();
		if (curGd == null)
			return null;
		
		GridData selGd = null;
		boolean bFound = false;
		for( int i = 0; i < getCount(); i++)
		{
			GridData gd = (GridData)getItem(i);
			if(!gd.isDirectory && gd.url != null && gd.url.equalsIgnoreCase(curGd.url))
				break;
			
			if(!gd.isDirectory)
			{
				bFound = true;
				selGd = gd;
			}
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
		Log.v(TAG, "=SelectItem= " + item);
		if (item == null)
			return false;
		
		if(item.isDirectory)
		{
			File f = new File(item.url);
			
			set_sel(item);
			if(item.isBack)
			{
				update_dir(f.getParentFile());
			}
			else
			{
				update_dir(f);
			}
			
			if(getCount() < 1)
			{
				set_root_dir();
				update_dir(null);
			}
			notifyDataSetChanged();
			return false;
		}
		
		set_sel(item);
		notifyDataSetInvalidated();
		return true;
	}

	@Override
	public boolean GoBackFromSelectedItem() 
	{
		GridData sel = get_sel();
		Log.v(TAG, "=GoBackFromSelectedItem= " + sel);
		if(sel == null)
			return false;

		GridData gd_prev = get_prev();
		if(gd_prev == null)
			return false;

		SelectItem(gd_prev);
		
		File f = new File(gd_prev.url);
		boolean is_root = (f != null && isRootDir(f.getParentFile()));
		if(is_root)
			set_sel(null);
		
		return true;
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
		ArrayList<GridData> searchableArr=null;

		if(MainActivity.screenMode!= MainActivity.ScreenMode.MultiView)
			searchableArr=itemList;
		else
			searchableArr=MainActivity._2x2camerasData;

		for(GridData item : searchableArr)
    		if(item.id == id)
    			return item; 

    	return null;
	}

	@Override
	public boolean AddItem(String name, String url, String user,
			String password, int channel_id, int preview, String image_file) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean UpdateItem(long id, String name, String url, String user,
			String password, int channel_id, int preview, String image_file) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean DeleteItem(long id) 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	final public static String TAG = "FilesList";
}