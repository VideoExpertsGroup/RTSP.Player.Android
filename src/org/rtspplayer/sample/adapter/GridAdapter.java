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
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import org.rtspplayer.sample.R;
import org.rtspplayer.sample.activity.MainActivity;
import org.rtspplayer.sample.data.GridData;
import org.rtspplayer.sample.data.GridData.ContentType;
import org.rtspplayer.sample.util.ShadowImage;

public abstract class GridAdapter extends BaseAdapter {
    public interface GridAdapterCallback {
        void onLoadComplete(boolean ret);
    }

    protected boolean storageSlot1Empty = true;
    protected boolean storageSlot2Empty = true;
    protected boolean storageSlot3Empty = true;
    protected boolean storageSlot4Empty = true;

    protected Context mContext;
    public MainActivity act;
    protected ArrayList<GridData> itemList = null;

    protected GridData m_sel = null;
    protected GridData m_prev = null;

    private ShadowImage drawableBlank = null;
    private float shadowRadius = 1f;
    private PointF shadowDirection = new PointF(1f, 1f);
    private float thumbWidth = 96f;
    private float thumbHeight = 72f;
    public ArrayList<GridData> checkedCams = new ArrayList<GridData>();
    protected GridAdapterCallback callback = null;

    public GridAdapter(Context c, GridAdapterCallback callback) {
        act = (MainActivity) c;
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
    public ArrayList<GridData> getList() {
        return itemList;
    }

    public int getEmptyStorageSlotIndex() {
        if (storageSlot1Empty)
            return 1;
        if (storageSlot2Empty)
            return 2;
        if (storageSlot3Empty)
            return 3;
        if (storageSlot4Empty)
            return 4;

        return -1;
    }

    public void setSlotBusy(int ind) {
        switch (ind) {
            case (1):
                storageSlot1Empty = false;
                break;
            case (2):
                storageSlot2Empty = false;
                break;
            case (3):
                storageSlot3Empty = false;
                break;
            case (4):
                storageSlot4Empty = false;
                break;
        }
    }

    public void setSlotEmpty(int ind) {
        switch (ind) {
            case (1):
                storageSlot1Empty = true;
                break;
            case (2):
                storageSlot2Empty = true;
                break;
            case (3):
                storageSlot3Empty = true;
                break;
            case (4):
                storageSlot4Empty = true;
                break;
        }
    }

    public void emptySlots() {
        storageSlot1Empty = true;
        storageSlot2Empty = true;
        storageSlot3Empty = true;
        storageSlot4Empty = true;
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
    public int getCount() {
        return itemList.size();
    }

    public Object getItem(int position) {
        return itemList.get(position);
    }

    public long getItemId(int position) {
        return itemList.get(position).id;
    }

    public void set_sel(GridData gd) {
        m_sel = gd;
    }

    public GridData get_sel() {
        return m_sel;
    }

    public GridData get_prev() {
        return m_prev;
    }

    public boolean is_sel_exist() {
        GridData sel = get_sel();
        if (sel == null || itemList == null || itemList.size() <= 0)
            return false;

        for (GridData gd : itemList)
            if (gd.id == sel.id)
                return true;

        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final int position_ = position;
        final View container;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            container = inflater.inflate(R.layout.item_row_doc, parent, false);
        } else {
            container = convertView;
        }

        GridData sel = get_sel();

        GridData item_prev = (GridData) container.getTag();
        final GridData item = itemList.get(position);

        ImageButton imbClose = (ImageButton) container.findViewById(R.id.view_grid_item_imb_close);
        imbClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        if (!item.DrawItem(mContext, container, thumbWidth, thumbHeight, shadowRadius, shadowDirection)) {
//            ImageView imbThumb = (ImageView) container.findViewById(R.id.document_item);
//            imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            
//			imbThumb.setImageDrawable(drawableBlank);
//        	imbThumb.setScaleType(ScaleType.FIT_CENTER);
            Draw(mContext, container, item.getContentType(), thumbWidth, thumbHeight, shadowRadius, shadowDirection);
        }
        final CheckBox checkBox = (CheckBox) container.findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final GridData clickedItem = (GridData) container.getTag();
                if (checkBox.isChecked()) {
                    clickedItem.checkBoxIndex = getEmptyStorageSlotIndex();
                    boolean maxSizeAcheaved=(act.currentList == act.filesList?checkedCams.size()==4:clickedItem.checkBoxIndex<0);
                    if (!maxSizeAcheaved) {
                        clickedItem.chosen = true;

                        checkedCams.add(clickedItem);
                        setSlotBusy(clickedItem.checkBoxIndex);
                        if (act.currentList == act.camerasList)
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("cam" + clickedItem.checkBoxIndex, item.name).commit();
                        if (act.currentList == act.streamsList)
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("stream" + clickedItem.checkBoxIndex, item.name).commit();
                        if (act.currentList == act.filesList)
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(mContext.getResources().getString(R.string.file) + clickedItem.checkBoxIndex, item.name).commit();
                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.max_chosen_item), Toast.LENGTH_LONG).show();
                        checkBox.setChecked(false);
                        clickedItem.chosen = false;
                    }


                } else {
                    String s = PreferenceManager.getDefaultSharedPreferences(mContext).getString("cam" + clickedItem.checkBoxIndex, null);

                    if (act.currentList == act.camerasList)
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("cam" + clickedItem.checkBoxIndex, null).commit();
                    if (act.currentList == act.streamsList)
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("stream" + clickedItem.checkBoxIndex, null).commit();
                    if (act.currentList == act.filesList)
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(mContext.getResources().getString(R.string.file) + clickedItem.checkBoxIndex, null).commit();
                    s = PreferenceManager.getDefaultSharedPreferences(mContext).getString("cam" + clickedItem.checkBoxIndex, null);
                    setSlotEmpty(clickedItem.checkBoxIndex);
                    checkedCams.remove(item);
                    clickedItem.checkBoxIndex = -1;
                    clickedItem.chosen = false;
                }
            }
        });

        checkBox.setChecked(((GridData) container.getTag()).chosen);

        checkBox.setVisibility(MainActivity.screenMode == MainActivity.ScreenMode.MultiView && !item.isDirectory ? View.VISIBLE : View.GONE);


        TextView tvTitle = (TextView) container.findViewById(R.id.document_text_1);
        tvTitle.setText(item.name);

        return container;
    }

    public boolean Draw(final Context context, final View view, final ContentType type, float thumbWidth, float thumbHeight, float shadowRadius, PointF shadowDirection) {
        ImageView imbThumb = (ImageView) view.findViewById(R.id.document_item);
        imbThumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        imbThumb.setImageDrawable(drawableBlank);
        imbThumb.setScaleType(ScaleType.FIT_CENTER);

        return true;
    }

    protected float dpFromPx(float px) {
        return (px / mContext.getResources().getDisplayMetrics().density);
    }

    protected float pxFromDp(float dp) {
        return (dp * mContext.getResources().getDisplayMetrics().density);
    }

    final public static String TAG = "GridAdapter";
}