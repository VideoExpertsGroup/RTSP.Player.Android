/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.data;

import java.io.File;
import java.util.Comparator;

import org.rtspplayer.sample.R;
import org.rtspplayer.sample.util.ShadowImage;
import org.rtspplayer.sample.util.SupportedFormats;

import veg.mediaplayer.sdk.Thumbnailer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class GridData 
{
	public enum ContentType
	{
		Video,
		Audio
	}

	public enum UpdateState
	{
		none,
		updating,
		update
	}
	public int checkBoxIndex=-1;
    public String name = "";
    public String url = "";
    public String user = "";
    public String password = "";
    public ContentType type = ContentType.Video;
    public long id;
    public int image;
    public boolean chosen=false;
    public String image_file = "";
    public boolean isBack = false; //back directory ".."
    public boolean isDirectory = false;
    public boolean isUpdate = false;
    
    public UpdateState update_state = UpdateState.none;
    public Thumbnailer thumbnailer = null;
    public Drawable draw = null;
    
    private GridData gd_prev = null;
    
    static public class GridDataComparator implements Comparator<GridData>
    {
        public int compare(GridData left, GridData right) 
        {
        	if(left.isDirectory == right.isDirectory)
        	{
        		return left.name.compareTo(right.name);
        	}
        	
        	if(left.isDirectory)
        		return (-1);
        	
        	return 1;
        }
    }

    public GridData(String name, String url, String user, String password, long id, int image, String image_file) 
    {
        this.name = name;
        this.url = url;
        this.user = user;
        this.password = password;
        this.id = id;
        this.image = image;
        this.image_file = image_file;
        this.update_state = UpdateState.none;
    }
    
    public void set_prev(GridData gd)
    {
    	gd_prev = gd;
    }
    
    public GridData get_prev()
    {
    	return gd_prev;
    }
    
    public ContentType getContentType()
    {
    	if (url == null || url.isEmpty())
    		return ContentType.Video;
    	
		for(String s : SupportedFormats.AUDIO_SSFORMATS)
			if(url.endsWith(s))
				return ContentType.Audio;
    	
    	return ContentType.Video;
    }
    
    public boolean DrawItem(final Context context, final View view, float thumbWidth, float thumbHeight, float shadowRadius, PointF shadowDirection)
    {
        ImageView imbThumb = (ImageView) view.findViewById(R.id.document_item);
        imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        if(image_file != null && !image_file.isEmpty())
        {
            File fimg = new File(image_file);
            if(fimg.exists())
            {
            	Bitmap bm;
            	BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
            	bm = BitmapFactory.decodeFile(fimg.getAbsolutePath(),btmapOptions);
            	if (bm == null)
            		return false;
            	
    			imbThumb.setImageDrawable(new ShadowImage(context.getResources(), bm, 
    										thumbWidth, thumbHeight, shadowDirection, shadowRadius));
            	imbThumb.setScaleType(ScaleType.FIT_CENTER);
            	return true;
            }
        }
        
    	return false;
    }


    public boolean equals(Object obj){
        if(obj!=null) {
            GridData cam = (GridData) obj;
                if(cam.name!=null)
                return cam.name.equals(name);
                else
                    return false;
        }
        else return false;
    }
   
}
