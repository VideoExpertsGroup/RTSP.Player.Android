/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.MediaPlayerCallback;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.VideoShot;
import org.rtspplayer.sample.R;
import org.rtspplayer.sample.activity.MainActivity;
import org.rtspplayer.sample.data.GridData;
import org.rtspplayer.sample.util.SharedSettings;


public class AddChannelDialog2 extends Dialog implements MediaPlayerCallback {
	
	public interface AddChannelDialogListener2 
	{
	    void onSaveAddChannelDialog(String name, String url, String user, String password, int channel_id, int preview, boolean is_shot_taken, String image_file);
	    void onCancelAddChannelDialog();
	}
	
	final public static String TAG = "AddChannelDialog";
	
	static public long IS_EDIT_ID = -1; //id of edited item
	String mFilePreview = "";
	String mCurUrl = "";
    //player
	private enum PlayerStates
	{
	  	Busy,
	  	ReadyForUse
	};
	PlayerStates player_state = PlayerStates.ReadyForUse;
	
	private enum PlayerStatesError
	{
	  	None,
	  	Disconnected
	};
	PlayerStatesError 	player_state_error = PlayerStatesError.None;
    private MediaPlayer player = null;
	
    private FrameLayout playerContainer = null;
	private ProgressBar progress_bar = null;
	private ImageView	picStatusDisconneted = null;
	private TextView 	tvPreview = null;
	private ImageView 	dialog_imv_preview = null;
	
	private boolean 	is_playing = false;
	private boolean 	is_showing = false;	
	private boolean 	is_shot_taken = false;
	private boolean 	with_preview = true;
	//settings
	SharedSettings settings = null;

	private EditText edName = null;
	private EditText edURL = null;
	
	private Activity	parent_activity  	= null;
	
	private String nameStart = "";
	//private String url = "http://tvrain-video.ngenix.net/mobile/TVRain_1m.stream/chunklist.m3u8";
	//private String url = "file:///storage/emulated/0/DCIM/Camera/VID_20140411_065215.mp4";
	private String urlStart = "";
	//private String url = "http://hls.cn.ru/streaming/ntv/tvrec/playlist.m3u8";
	//private String urlStart = "http://tv.life.ru/lifetv/480p/index.m3u8";

    public AddChannelDialog2(Activity activity, final boolean with_preview) 
    {
		//super(activity, R.style.AddChannelDialogTheme);
		super(activity);
    	this.with_preview = with_preview;
		this.parent_activity = activity;
    }
    
	public void setContentValues(final String name, final String url)
	{
		nameStart = name;
		urlStart = url;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() 
	{
    	mFilePreview = "";
    	mCurUrl = "";
        player = null;
        player_state_error = PlayerStatesError.None;
        
        final ImageView imgView = (ImageView) findViewById(R.id.dialog_img_ico);
        imgView.setImageResource((IS_EDIT_ID==(-1))?R.drawable.pl : R.drawable.modify);

        edName = (EditText) findViewById(R.id.dialog_ed_name);
        edName.setText(nameStart);
        edName.setHint(parent_activity.getResources().getString(R.string.dialog_name_hint));

        
        //Spinner spinner = (Spinner) container.findViewById(R.id.dialog_spinner);
        edURL = (EditText) findViewById(R.id.dialog_ed_url);
        edURL.setText(urlStart);
        edURL.setHint(parent_activity.getResources().getString(R.string.dialog_url_hint));
             
        edURL.setOnKeyListener(new View.OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode,
					KeyEvent event) {
				Log.i(TAG, "=onKey keyCode="+keyCode);
				if(is_playing){
					setPlaying(false);
				}
				return false;
			}
        	
        });
        //final EditText edUser = (EditText) container.findViewById(R.id.dialog_ed_user);
        //final EditText edPassword = (EditText) container.findViewById(R.id.dialog_ed_password);
        //final ImageView imv = (ImageView) container.findViewById(R.id.dialog_imv);
        
        if (with_preview)
        {
        	progress_bar = (ProgressBar) findViewById(R.id.layout_hide_progress);
			picStatusDisconneted = (ImageView) findViewById(R.id.pic_status_disconnected);
			Drawable d = picStatusDisconneted.getDrawable();
			d.setLevel(5000);
			
	        tvPreview = (TextView) findViewById(R.id.dialog_status_preview);
	        tvPreview.setText(R.string.dialog_preview_first);
	        
			playerContainer = (FrameLayout) findViewById(R.id.playerViewPreviewContainer);
	        player = (MediaPlayer)findViewById(R.id.playerViewPreview);
	        
			hideProgressView();

			player.getConfig().setDataReceiveTimeout(20000000);
	        player.backgroundColor(Color.parseColor("#FFEFEFEF"));
        }
        
        final Button butSave = (Button) findViewById(R.id.dialog_but_positive);
        final Button butCancel = (Button) findViewById(R.id.dialog_but_negative);

       	butSave.setEnabled(!nameStart.isEmpty() && !urlStart.isEmpty());
        
        edURL.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) 
            {
            	final String nameSave = edName.getText().toString();
                if (s == null || s.length() == 0 ||
                		nameSave == null || nameSave.length() == 0) 
                {
                   butSave.setEnabled(false);
                }
                else 
                {
                	butSave.setEnabled(true);
                }
            }
        });
        
        edName.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) 
            {
            	final String urlSave = edURL.getText().toString();
                if (s == null || s.length() == 0 ||
                		urlSave == null || urlSave.length() == 0) 
                {
                   butSave.setEnabled(false);
                }
                else 
                {
                	butSave.setEnabled(true);
                }
            }
        });

        String[] data = parent_activity.getResources().getStringArray(R.array.channel_type_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent_activity, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);

        /*
        Random random = new Random();
        int imagesRef[] = {R.drawable.im1, R.drawable.im2, R.drawable.im3, R.drawable.im4, R.drawable.im5};
        final int randomRef = imagesRef[random.nextInt(5)];
        final int randomId = random.nextInt(1000);
        */
        final int imagesClip = R.drawable.ic_pl_2;

        if (with_preview)
        {	
        	dialog_imv_preview = (ImageView) findViewById(R.id.dialog_imv_preview);
	        dialog_imv_preview.setImageResource(imagesClip);
			dialog_imv_preview.setVisibility(View.INVISIBLE);
			is_shot_taken = false;
	        setPlaying(false);
        
	        player.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					
					Log.v(TAG, "=onClick is_playing="+is_playing+" player_state="+player_state);
					if(player_state == PlayerStates.Busy)
						return;
					
	            	final String urlTest = edURL.getText().toString();
					if(urlTest.isEmpty())
						return;
					
					if(!is_playing){
						playerConnect(edURL.getText().toString());
					}else{
						
					    File mediaStorageDir = new File(parent_activity.getExternalFilesDir(null), parent_activity.getResources().getString(R.string.storage_thumbs));
	
					    // This location works best if you want the created images to be shared
					    // between applications and persist after your app has been uninstalled.
	
					    // Create the storage directory if it does not exist
					    if (! mediaStorageDir.exists()){
					    	if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
								Log.e(TAG, "<=error mediaStorageDir can't created"+mediaStorageDir);
					    		return;
					    	}
					    }
					    
					    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
						
					    String image_file;
					    image_file = (mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
					    
	
					    VideoShot vs = player.getVideoShot(-1, -1);
	
						if(vs != null && take_snapshot(image_file, vs.getData(), vs.getWidth(), vs.getHeight())){
						    //remove prev
						    if(mFilePreview.length()>0){
							    File f = new File(mFilePreview);
							    if(f.exists()){
							    	f.delete();
							    }
							    mFilePreview = "";
						    }
							
							mFilePreview = image_file; 
							
							if(mFilePreview.length()>0){
								File f = new File(mFilePreview);
							    if(f.exists()){
							    	Bitmap bm;
							    	BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
							    	bm = BitmapFactory.decodeFile(f.getAbsolutePath(),btmapOptions);
							    	dialog_imv_preview.setImageBitmap(bm);
							    	dialog_imv_preview.setScaleType(ScaleType.CENTER_INSIDE);
					    			dialog_imv_preview.setVisibility(View.VISIBLE);
					    			is_shot_taken = true;
							    }
							}
	
						}
					}
				}
	        });
        }
        
        butSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
//                ((MainActivity) getActivity()).addChannel(edName.getText().toString(),
//                        edURL.getText().toString(), ""/*edUser.getText().toString()*/,
//                        ""/*edPassword.getText().toString()*/, 1, 1, mFilePreview);
            	butSave.requestFocus();
            	
            	final String urlSave = edURL.getText().toString();
            	final String nameSave = edName.getText().toString();
            	
            	MainActivity main = (MainActivity) parent_activity;
            	if (main != null && !urlSave.isEmpty() && main.isUrlExist(urlSave) && AddChannelDialog.IS_EDIT_ID == (-1))
            	{
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(parent_activity);
                    alertDialog.setMessage(parent_activity.getResources().getString(R.string.dialog_url_exist_message));

                    alertDialog.setPositiveButton(parent_activity.getResources().getString(R.string.dialog_url_exist_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	saveChannel(nameSave, urlSave);
                        }
                    });

                    alertDialog.setNegativeButton(parent_activity.getResources().getString(R.string.dialog_url_exist_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.show();
                    return;
            	}	
            	
            	saveChannel(nameSave, urlSave);
            }
        });

        butCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	butCancel.requestFocus();
            	setPlaying(false);
            	cancel();

                AddChannelDialogListener2 activity = (AddChannelDialogListener2)parent_activity;
            	if (activity != null)
            		activity.onCancelAddChannelDialog();                
            }
        });

    	//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    	setCanceledOnTouchOutside(false);
	}
	
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//    	//default
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//    	//with theme
//        //AlertDialog.Builder builder = new AlertDialog.Builder(
//        //		new ContextThemeWrapper(getActivity(), R.style.AddChannelDialogTheme));
//        //builder.setTitle(R.string.app_name);
//        //builder.setTitle(R.string.dialog_title);
//        //builder.setMessage(R.string.dialog_title);
//        //builder.setCancelable(false);
//        
//    	mFilePreview = "";
//    	mCurUrl = "";
//        player = null;
//        player_state_error = PlayerStatesError.None;
//        
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View container = inflater.inflate(with_preview ? R.layout.dialog : R.layout.dialog_no_preview, null);
//        builder.setView(container);
//        
//        final ImageView imgView = (ImageView) container.findViewById(R.id.dialog_img_ico);
//        imgView.setImageResource((IS_EDIT_ID==(-1))?R.drawable.pl : R.drawable.modify);
//
//        edName = (EditText) container.findViewById(R.id.dialog_ed_name);
//        edName.setText(nameStart);
//        edName.setHint(getResources().getString(R.string.dialog_name_hint));
//
//        
//        //Spinner spinner = (Spinner) container.findViewById(R.id.dialog_spinner);
//        edURL = (EditText) container.findViewById(R.id.dialog_ed_url);
//        edURL.setText(urlStart);
//        edURL.setHint(getResources().getString(R.string.dialog_url_hint));
//             
//        edURL.setOnKeyListener(new OnKeyListener(){
//
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				
//				Log.i(TAG, "=onKey keyCode="+keyCode);
//				if(is_playing){
//					setPlaying(false);
//				}
//				return false;
//			}
//        	
//        });
//        //final EditText edUser = (EditText) container.findViewById(R.id.dialog_ed_user);
//        //final EditText edPassword = (EditText) container.findViewById(R.id.dialog_ed_password);
//        //final ImageView imv = (ImageView) container.findViewById(R.id.dialog_imv);
//        
//        if (with_preview)
//        {
//        	progress_bar = (ProgressBar) container.findViewById(R.id.layout_hide_progress);
//			picStatusDisconneted = (ImageView) container.findViewById(R.id.pic_status_disconnected);
//			Drawable d = picStatusDisconneted.getDrawable();
//			d.setLevel(5000);
//			
//	        tvPreview = (TextView) container.findViewById(R.id.dialog_status_preview);
//	        tvPreview.setText(R.string.dialog_preview_first);
//	        
//			playerContainer = (FrameLayout) container.findViewById(R.id.playerViewPreviewContainer);
//	        player = (MediaPlayer)container.findViewById(R.id.playerViewPreview);
//	        
//			hideProgressView();
//
//			MediaPlayer.DataReceiveTimeout = 20000000;
//	        player.backgroundColor(Color.parseColor("#FFEFEFEF"));
//        }
//        
//        final Button butSave = (Button) container.findViewById(R.id.dialog_but_positive);
//        final Button butCancel = (Button) container.findViewById(R.id.dialog_but_negative);
//
//       	butSave.setEnabled(!nameStart.isEmpty() && !urlStart.isEmpty());
//        
//        edURL.addTextChangedListener(new TextWatcher() {
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            public void afterTextChanged(Editable s) 
//            {
//            	final String nameSave = edName.getText().toString();
//                if (s == null || s.length() == 0 ||
//                		nameSave == null || nameSave.length() == 0) 
//                {
//                   butSave.setEnabled(false);
//                }
//                else 
//                {
//                	butSave.setEnabled(true);
//                }
//            }
//        });
//        
//        edName.addTextChangedListener(new TextWatcher() {
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            public void afterTextChanged(Editable s) 
//            {
//            	final String urlSave = edURL.getText().toString();
//                if (s == null || s.length() == 0 ||
//                		urlSave == null || urlSave.length() == 0) 
//                {
//                   butSave.setEnabled(false);
//                }
//                else 
//                {
//                	butSave.setEnabled(true);
//                }
//            }
//        });
//
//        String[] data = getResources().getStringArray(R.array.channel_type_array);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        //spinner.setAdapter(adapter);
//
//        /*
//        Random random = new Random();
//        int imagesRef[] = {R.drawable.im1, R.drawable.im2, R.drawable.im3, R.drawable.im4, R.drawable.im5};
//        final int randomRef = imagesRef[random.nextInt(5)];
//        final int randomId = random.nextInt(1000);
//        */
//        final int imagesClip = R.drawable.ic_pl_2;
//
//        if (with_preview)
//        {	
//        	dialog_imv_preview = (ImageView) container.findViewById(R.id.dialog_imv_preview);
//	        dialog_imv_preview.setImageResource(imagesClip);
//			dialog_imv_preview.setVisibility(View.INVISIBLE);
//			is_shot_taken = false;
//	        setPlaying(false);
//        
//	        player.setOnClickListener(new OnClickListener(){
//				@Override
//				public void onClick(View v) {
//					
//					Log.v(TAG, "=onClick is_playing="+is_playing+" player_state="+player_state);
//					if(player_state == PlayerStates.Busy)
//						return;
//					
//	            	final String urlTest = edURL.getText().toString();
//					if(urlTest.isEmpty())
//						return;
//					
//					if(!is_playing){
//						playerConnect(edURL.getText().toString());
//					}else{
//						
//						if(MainActivity.this_ == null){
//							Log.e(TAG, "<=error MainActivity.this_ == null!!!");
//							return;
//						}
//					    File mediaStorageDir = new File(MainActivity.this_.getExternalFilesDir(null), MainActivity.STOR_THUMBS);
//	
//					    // This location works best if you want the created images to be shared
//					    // between applications and persist after your app has been uninstalled.
//	
//					    // Create the storage directory if it does not exist
//					    if (! mediaStorageDir.exists()){
//					    	if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
//								Log.e(TAG, "<=error mediaStorageDir can't created"+mediaStorageDir);
//					    		return;
//					    	}
//					    }
//					    
//					    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//						
//					    String image_file;
//					    image_file = (mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
//					    
//	
//					    VideoShot vs = player.getVideoShot(-1, -1);
//	
//						if(vs != null && take_snapshot(image_file, vs.getData(), vs.getWidth(), vs.getHeight())){
//						    //remove prev
//						    if(mFilePreview.length()>0){
//							    File f = new File(mFilePreview);
//							    if(f.exists()){
//							    	f.delete();
//							    }
//							    mFilePreview = "";
//						    }
//							
//							mFilePreview = image_file; 
//							
//							if(mFilePreview.length()>0){
//								File f = new File(mFilePreview);
//							    if(f.exists()){
//							    	Bitmap bm;
//							    	BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
//							    	bm = BitmapFactory.decodeFile(f.getAbsolutePath(),btmapOptions);
//							    	dialog_imv_preview.setImageBitmap(bm);
//							    	dialog_imv_preview.setScaleType(ScaleType.CENTER_INSIDE);
//					    			dialog_imv_preview.setVisibility(View.VISIBLE);
//					    			is_shot_taken = true;
//							    }
//							}
//	
//						}
//					}
//				}
//	        });
//        }
//        
//        butSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            	
////                ((MainActivity) getActivity()).addChannel(edName.getText().toString(),
////                        edURL.getText().toString(), ""/*edUser.getText().toString()*/,
////                        ""/*edPassword.getText().toString()*/, 1, 1, mFilePreview);
//            	butSave.requestFocus();
//            	
//            	final String urlSave = edURL.getText().toString();
//            	final String nameSave = edName.getText().toString();
//            	
//            	MainActivity main = (MainActivity) getActivity();
//            	if (main != null && !urlSave.isEmpty() && main.isUrlExist(urlSave) && AddChannelDialog2.IS_EDIT_ID == (-1))
//            	{
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
//                    alertDialog.setMessage(getResources().getString(R.string.dialog_url_exist_message));
//
//                    alertDialog.setPositiveButton(getResources().getString(R.string.dialog_url_exist_yes), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                        	saveChannel(nameSave, urlSave);
//                        }
//                    });
//
//                    alertDialog.setNegativeButton(getResources().getString(R.string.dialog_url_exist_no), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//
//                    alertDialog.show();
//                    return;
//            	}	
//            	
//            	saveChannel(nameSave, urlSave);
//            }
//        });
//
//        butCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            	butCancel.requestFocus();
//            	setPlaying(false);
//            	getDialog().cancel();
//
//                AddChannelDialogListener activity = (AddChannelDialogListener)getActivity();
//            	if (activity != null)
//            		activity.onCancelAddChannelDialog();                
//            }
//        });
//
//        AlertDialog this_dlg = builder.create();
//        if (this_dlg != null && this_dlg.getWindow() != null) // diasabled dims effect
//        {
//        	this_dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        	this_dlg.setCanceledOnTouchOutside(false);
//        }
//        
//        return this_dlg;
//    }
    
//    @Override
//    public void onCancel(DialogInterface dialog){
//    	Log.v(TAG, "=>onCancel");
//    	//setPlaying(false);
//    	super.onCancel(dialog);
//    	Log.v(TAG, "<=onCancel");
//    }
    
    public boolean isShowing() { return is_showing; };
    public boolean isShotTaken() { return is_shot_taken; };
    public boolean refreshPlayer() {
    	if (player != null)
    		player.requestFocus();
    	
//        LayoutInflater inflater = parent_activity.getLayoutInflater();
//        View container = inflater.inflate(with_preview ? R.layout.dialog : R.layout.dialog_no_preview, null);
        
        final ImageView imgView = (ImageView) findViewById(R.id.dialog_img_ico);
        imgView.setImageResource((IS_EDIT_ID==(-1))?R.drawable.pl : R.drawable.modify);
        
        setPlaying(false);
        return true;
    }
    
//    @Override
//    public void show(FragmentManager manager, String tag) {
//        if (is_showing) return;
//
//        super.show(manager, tag);
//        is_showing = true;
//    }
//
//    @Override
//    public void onDismiss(DialogInterface dialog) {
//    	is_showing = false;
//		nameStart = "";
//		urlStart = "";
//        super.onDismiss(dialog);
//    }
    
    
    public void setPlaying(boolean playing){
    	is_playing = playing;
    	
    	if(is_playing){
    		if(tvPreview != null)
    			tvPreview.setText(R.string.dialog_preview_snapshot);
    		//if(progress_bar != null)
    		//	progress_bar.setVisibility(View.INVISIBLE);
//    		if(dialog_imv_preview != null)
//    			dialog_imv_preview.setVisibility(View.VISIBLE);
    	}else{
			is_shot_taken = false;
    		if(dialog_imv_preview != null)
    		{
    			dialog_imv_preview.setVisibility(View.INVISIBLE);
    		}
    		//if(progress_bar != null)
    		//	progress_bar.setVisibility(View.VISIBLE);
    		if(tvPreview != null)
    			tvPreview.setText(R.string.dialog_preview_first);
    		if(player != null)
    			player.Close();
    	}

    }
    

    public void playerConnect(String url){
    	if(player == null || url == null)
    		return;
    	

    	setPlaying(false);
		//if(progress_bar != null)
		//	progress_bar.setVisibility(View.VISIBLE);
       	player_state = PlayerStates.ReadyForUse;
       	
       	mCurUrl = url;
       	
 		//int decoderType = 0; //settings.getInt("decoderType", 0);
 		//int rendererType = 1; //settings.getInt("rendererType", 1);
 		//int rendererEnableColorVideo = 1; //settings.getInt("rendererEnableColorVideo", 1);
 		//int rendererEnableAspectRatio = 1; //settings.getInt("rendererEnableAspectRatio", 1);
 		//int synchroEnable = 1; //settings.getInt("synchroEnable", 1);
 		//int synchroNeedDropVideoFrames = 1; //settings.getInt("synchroNeedDropVideoFrames", 1);
 		
 		
 		Log.i(TAG, "=playerConnect url="+url);
 		
 		//String strText = "Connecting...";
 		//Toast.makeText(this, strText, Toast.LENGTH_SHORT).show();
 		
        // Connect and start playback
 		player.OpenAsPreview(url, player.getConfig().getDataReceiveTimeout(), this);
        /*player.Open(gd.url, 
        		decoderType,
        		rendererType,
        		synchroEnable, 
        		synchroNeedDropVideoFrames,
        		rendererEnableColorVideo, 
        		rendererEnableAspectRatio, 
        		MediaPlayer.DataReceiveTimeout, 
        		this);*/
 		
 		//player.setAlpha(1);
    	
    }
    
    private int mOldMsg = 0;
	private Object waitOnMe = new Object();
	private Handler handler = new Handler() 
    {
		String strText = "Status:";
		
        @Override
        public void handleMessage(Message msg) 
        {
        	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	    	Log.e(TAG, "Notify: " + status);
	    	
	    	switch (status){
        	case CP_CONNECT_STARTING:
        		/*
        		if (reconnect_type == PlayerConnectType.Reconnecting)
        			strText = "Reconnecting";
        		else
        			strText = "Connecting";
        			*/
        			
        		//startProgressTask(strText);
        		
        		player_state = PlayerStates.Busy;
        		player_state_error = PlayerStatesError.None;
        		showProgressView();
    			//showStatusView();
    			
    			//reconnect_type = PlayerConnectType.Normal;
    			
    			//if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				//admob.startShow();
    			
    			break;
                
	    	case VRP_NEED_SURFACE:
	    		player_state = PlayerStates.Busy;
	    		//showVideoView();
		        synchronized (waitOnMe) { waitOnMe.notifyAll(); }
				break;

	    	case PLP_PLAY_SUCCESSFUL:
	    		player_state = PlayerStates.ReadyForUse;
	    		player_state_error = PlayerStatesError.None;
	    		hideProgressView();
	    		setPlaying(true);
	    		
        		//stopProgressTask();
    			//playerStatusText.setText("");
	     		//player.setAlpha(1.0f);
		        //synchronized (waitOnMe) { waitOnMe.notifyAll(); }
	     		//startNetTask();
		        break;
                
        	case PLP_CLOSE_STARTING:
        		player_state = PlayerStates.Busy;
        		//stopProgressTask();
    			//playerStatusText.setText("Disconnected");
    			//showStatusView();
    			//stopNetTask();
                break;
                
        	case PLP_CLOSE_SUCCESSFUL:
        		player_state = PlayerStates.ReadyForUse;
        		hideProgressView();
        		//stopProgressTask();
    			//playerStatusText.setText("Disconnected");
    			//showStatusView();
    			
    			//if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    			//	admob.stopShow();
    			
    			//stopNetTask();
    			System.gc();
                break;
                
        	case PLP_CLOSE_FAILED:
        		player_state = PlayerStates.ReadyForUse;
        		hideProgressView();
        		//stopProgressTask();
    			//playerStatusText.setText("Disconnected");
    			//showStatusView();
    			
    			//if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				//admob.stopShow();
                
    			//stopNetTask();
   			break;
               
        	case CP_CONNECT_FAILED:
        		player_state = PlayerStates.ReadyForUse;
        		player_state_error = PlayerStatesError.Disconnected;
        		hideProgressView();
        		//stopProgressTask();
    			//playerStatusText.setText("Disconnected");
    			//showStatusView();
    			
    			//if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				//admob.stopShow();

    			//stopNetTask();
    			break;
                
            case PLP_BUILD_FAILED:
            	player_state = PlayerStates.ReadyForUse;
        		player_state_error = PlayerStatesError.Disconnected;
            	hideProgressView();
        		/*stopProgressTask();
    			playerStatusText.setText("Disconnected");
    			showStatusView();
                
    			if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				admob.stopShow();

    			stopNetTask();*/
    			break;
                
            case PLP_PLAY_FAILED:
            	player_state = PlayerStates.ReadyForUse;
        		player_state_error = PlayerStatesError.Disconnected;
            	hideProgressView();
        		/*stopProgressTask();
    			playerStatusText.setText("Disconnected");
    			showStatusView();
                
    			if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				admob.stopShow();

    			stopNetTask();*/
    			break;
                
            case PLP_ERROR:
            	player_state = PlayerStates.ReadyForUse;
        		player_state_error = PlayerStatesError.Disconnected;
            	hideProgressView();
        		/*stopProgressTask();
    			playerStatusText.setText("Disconnected");
    			showStatusView();
                
    			if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				admob.stopShow();

    			stopNetTask();*/
    			break;
                
            case CP_INTERRUPTED:
            	player_state = PlayerStates.ReadyForUse;
        		//player_state_error = PlayerStatesError.Disconnected;
            	hideProgressView();
        		/*stopProgressTask();
    			playerStatusText.setText("Disconnected");
    			showStatusView();
                
    			if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
    				admob.stopShow();

    			stopNetTask();*/
    			break;
                
            //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
            case CP_STOPPED:
            case VDP_STOPPED:
            case VRP_STOPPED:
            case ADP_STOPPED:
            case ARP_STOPPED:
            	if (player_state != PlayerStates.Busy)
            	{
	        		//stopProgressTask();
            		player_state = PlayerStates.Busy;
        			Log.e(TAG, "AUDIO_RENDERER_PROVIDER_STOPPED_THREAD Close.");
            		player.Close();
        			//playerStatusText.setText("Disconnected");
	    			//showStatusView();
	    			player_state = PlayerStates.ReadyForUse;
            	
        			//if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
        				//admob.stopShow();
        			
	    			//stopNetTask();
            	}
                break;

            case CP_ERROR_DISCONNECTED:
            	if (player_state != PlayerStates.Busy)
            	{
//            		state = PlayerStates.Busy;
//            		player.Close();
//        			playerStatusText.setText("Disconnected.");
//	    			showStatusView();
//            		state = PlayerStates.ReadyForUse;
            		
            		// if we want reconnect
	        		//stopProgressTask();
            		player_state = PlayerStates.Busy;
        			Log.e(TAG, "CONTENT_PROVIDER_ERROR_DISCONNECTED Close.");
            		player.Close();
        			//progress_bar.setVisibility(View.INVISIBLE);
        			//playerStatusText.setText("Reconnecting...");

        			//if (admob != null && SharedSettings.getInstance().AdShowWithCloseButton && chs_list.isAdRestartOnConnect()) 
        				//admob.stopShow();
            		
	    			//stopNetTask();

            		//RECONNECT
	    			//reconnect_type = PlayerConnectType.Reconnecting;
        	    	Log.e(TAG, "Reconnecting: " + player.getConfig().getDataReceiveTimeout());
        	    	
        	    	playerConnect(mCurUrl);
        	    	/*SharedSettings sett = SharedSettings.getInstance();
        	    	sett.decoderType = guardedByFunctionalityIntValue(sett.decoderType);
        			player.Open(MediaPlayer.ConnectionUrl, 
        					sett.decoderType, sett.rendererType, sett.synchroEnable, sett.synchroNeedDropVideoFrames, sett.rendererEnableColorVideo, 
        					guardedByOrientationIntValue(sett.rendererEnableAspectRatio), MediaPlayer.DataReceiveTimeout, mthis);
        					*/
            	}
                break;
                
            //case CONTENT_PROVIDER_FAILED_INIT:
//            case VIDEO_DECODER_PROVIDER_FAILED_INIT:
//            case VIDEO_RENDERER_PROVIDER_FAILED_INIT:
//            case AUDIO_DECODER_PROVIDER_FAILED_INIT:
//            case AUDIO_RENDERER_PROVIDER_FAILED_INIT:
//        		player.Close();
//        		state = PlayerStates.ReadyForUse;
//    			playerStatusText.setText("Disconnected.");
//        		playerviewAnimator.setDisplayedChild(1);
//                break;

            default:
            	player_state = PlayerStates.Busy;
    			//stopNetTask();
    			//playerStatusText.setText("");
        		//playerviewAnimator.setDisplayedChild(0);
        }
    		
	    strText += " "+status;
        //Toast.makeText(MainActivity.this, strText, Toast.LENGTH_SHORT).show();
        }
        
	};
    
	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public int Status(int arg0) {
		Log.i(TAG, "=Status arg="+arg0);
		
    	PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg0);
    	if (handler == null)
    		return 0;
    	
	    switch (status) 
	    {
	    	// for synchronus process
			//case PLAY_SUCCESSFUL:
	    	case VRP_NEED_SURFACE:
	    		synchronized (waitOnMe) 
	    		{
					Message msg = new Message();
					msg.obj = status;
					handler.sendMessage(msg);
	    		    try 
	    		    {
	    		        waitOnMe.wait();
	    		    }
	    		    catch (InterruptedException e) {}
	    		}			
				break;
	            
        	case CP_CONNECT_FAILED:
            case PLP_BUILD_FAILED:
            case PLP_PLAY_FAILED:
            case PLP_ERROR:
            case CP_ERROR_DISCONNECTED:
            {
        		player_state_error = PlayerStatesError.Disconnected;
            	Message msg = new Message();
	           	msg.obj = status;
	           	msg.what = 1;
	           	handler.removeMessages(mOldMsg);
	           	mOldMsg = msg.what;
	           	handler.sendMessage(msg);
                break;
            }

			// for asynchronus process
	        default:     
	        {
	        	Message msg = new Message();
	           	msg.obj = status;
	           	msg.what = 1;
	           	handler.removeMessages(mOldMsg);
	           	mOldMsg = msg.what;
	           	handler.sendMessage(msg);
	        }
	    }

		return 0;
	}
	
	boolean take_snapshot(String image_file, ByteBuffer frame, int width, int height)
	{
		Log.v(TAG, "=>take_snapshot "+width+"x"+"height, file="+image_file);
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		frame.rewind();
		bm.copyPixelsFromBuffer(frame);
		
		String sJPEG = image_file;
		FileOutputStream fos=null;
		try {
			fos = new FileOutputStream(sJPEG+"_");
			bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File f = new File(sJPEG+"_");
		File f2 = new File(sJPEG);
		boolean f_success = false;
		if(f.exists()){
			f_success = f.renameTo(f2);
		}

		if (bm != null)
			bm.recycle();
		Log.v(TAG, "<=take_snapshot res="+f_success);
		return f_success;
	}

	private void saveChannel(String name, String url) 
	{
		boolean was_shot_taken = is_shot_taken;
		setPlaying(false);
		cancel();
	
	    AddChannelDialogListener2 activity = (AddChannelDialogListener2)parent_activity;
		if (activity != null)
			activity.onSaveAddChannelDialog(name, url, "", "", 1, 1, was_shot_taken, mFilePreview);      
	}
	
	private void hideProgressView() 
	{
    	progress_bar.setVisibility(View.INVISIBLE);
   		picStatusDisconneted.setVisibility((player_state_error == PlayerStatesError.Disconnected) ? View.VISIBLE : View.INVISIBLE);
	}

	private void showProgressView() 
	{
    	progress_bar.setVisibility(View.VISIBLE);
		picStatusDisconneted.setVisibility(View.INVISIBLE);
	}

}