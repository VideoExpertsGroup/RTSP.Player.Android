/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import org.rtspplayer.sample.R;
import org.rtspplayer.sample.activity.MainActivity;
import org.rtspplayer.sample.data.GridData;
import org.rtspplayer.sample.data.GridData.ContentType;
import org.rtspplayer.sample.util.ShadowImage;

public abstract class GridAdapter extends BaseAdapter 
{
	public interface GridAdapterCallback 
	{
	    void onLoadComplete(boolean ret);
	}

	protected Context mContext;
    
    protected ArrayList<GridData> itemList = null;
    
    protected GridData m_sel = null;
    protected GridData m_prev = null;
    
    private ShadowImage drawableBlank = null;
    private float shadowRadius = 1f;
	private PointF shadowDirection = new PointF(1f, 1f);
    private float thumbWidth = 96f;
    private float thumbHeight = 72f;

    protected GridAdapterCallback callback = null;
    public GridAdapter(Context c, GridAdapterCallback callback) 
    {
        mContext = c;
        this.callback = callback;
        
        thumbWidth = pxFromDp(thumbWidth);
        thumbHeight = pxFromDp(thumbHeight);
        shadowDirection = new PointF(pxFromDp(shadowDirection.x), pxFromDp(shadowDirection.y));
        shadowRadius = pxFromDp(shadowRadius);
        drawableBlank = new ShadowImage(mContext.getResources(), BitmapFactory.decodeResource(c.getResources(), R.drawable.blank), 
        						thumbWidth, thumbHeight, shadowDirection, shadowRadius);
        
        itemList = new ArrayList<GridData>();
    }
    
    // Public interface
    public ArrayList<GridData> getList() 
    {
        return itemList;
    }
    
    public abstract boolean Load();
    public abstract boolean Refresh(); 
    public abstract boolean Close();

    public abstract boolean StartThumbnailUpdate();
    public abstract boolean StopThumbnailUpdate();
    
    public abstract GridData getFirstItem(); 
    public abstract GridData getSelectedItem(); 
    public abstract GridData getNextSelectedItem(long id); 
    public abstract GridData getPreviousSelectedItem(long id); 
    
    public abstract GridData getItem(long id); 

    public abstract boolean SelectItem(long id); 
    public abstract boolean SelectItem(GridData item); 
    
    public abstract boolean GoBackFromSelectedItem();
    
    public abstract boolean isItemExistByUrl(final String url); 

    public abstract boolean AddItem(String name, String url, String user, String password, int channel_id, int preview, String image_file); 
    public abstract boolean UpdateItem(long id, String name, String url, String user, String password, int channel_id, int preview, String image_file); 
    public abstract boolean DeleteItem(long id); 
    
    
    // Adapter interface
    public int getCount() 
    {
        return itemList.size();
    }

    public Object getItem(int position) 
    {
        return itemList.get(position);
    }

    public long getItemId(int position) 
    {
        return itemList.get(position).id;
    }
    
    public void set_sel(GridData gd)
    {
    	m_sel = gd;
    }
    
    public GridData get_sel()
    {
    	return m_sel; 
    }
    
    public GridData get_prev()
    {
    	return m_prev;
    }

    public boolean is_sel_exist()
    {
        GridData sel = get_sel();
        if (sel == null || itemList == null || itemList.size() <= 0)
        	return false;
        
    	for(GridData gd : itemList)
    		if(gd.id == sel.id)
            	return true;
    	
        return false; 
    }
    
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        final int position_ = position;
        View container;
        if (convertView == null) 
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            container = inflater.inflate(R.layout.item_row_doc, parent, false);
        } 
        else 
        {
            container = convertView;
        }
        
        GridData sel = get_sel();
        
        GridData item_prev = (GridData) container.getTag();
        GridData item = itemList.get(position);
        
        ImageButton imbClose = (ImageButton) container.findViewById(R.id.view_grid_item_imb_close);
        imbClose.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
                ((MainActivity) mContext).onDeleteChannel(itemList.get(position_));
            }
        });

        if (((MainActivity) mContext).mCloseIconsIsVisible)
            imbClose.setVisibility(View.VISIBLE);
        else
            imbClose.setVisibility(View.INVISIBLE);

//        if(mState == TabState.State_Files && item_prev != null && item != null &&
//        		item_prev.url.equals(item.url) && !item.isUpdate){
//        	//item the same
//        	return container;
//        }
        
        container.setTag(item);
        
        if (!item.DrawItem(mContext, container, thumbWidth, thumbHeight, shadowRadius, shadowDirection))
        {
//            ImageView imbThumb = (ImageView) container.findViewById(R.id.document_item);
//            imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            
//			imbThumb.setImageDrawable(drawableBlank);
//        	imbThumb.setScaleType(ScaleType.FIT_CENTER);
        	Draw(mContext, container, item.getContentType(), thumbWidth, thumbHeight, shadowRadius, shadowDirection);
        }
        
        TextView tvTitle = (TextView) container.findViewById(R.id.document_text_1);
        tvTitle.setText(item.name);

        return container;
    }
    
    public boolean Draw(final Context context, final View view, final ContentType type, float thumbWidth, float thumbHeight, float shadowRadius, PointF shadowDirection)
    {
        ImageView imbThumb = (ImageView) view.findViewById(R.id.document_item);
        imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
		imbThumb.setImageDrawable(drawableBlank);
    	imbThumb.setScaleType(ScaleType.FIT_CENTER);
        
    	return true;
    }
    
	protected float dpFromPx(float px)
	{
	    return (px / mContext.getResources().getDisplayMetrics().density);
	}

	protected float pxFromDp(float dp)
	{
	    return (dp * mContext.getResources().getDisplayMetrics().density);
	}    	
	
	final public static String TAG = "GridAdapter";
}