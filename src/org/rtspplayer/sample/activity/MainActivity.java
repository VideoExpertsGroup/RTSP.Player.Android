/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.MediaPlayerCallback;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayer.Position;

import org.rtspplayer.sample.R;
import org.rtspplayer.sample.adapter.CamerasList;
import org.rtspplayer.sample.adapter.FilesList;
import org.rtspplayer.sample.adapter.GridAdapter;
import org.rtspplayer.sample.adapter.GridAdapter.GridAdapterCallback;
import org.rtspplayer.sample.adapter.StreamsList;
import org.rtspplayer.sample.data.GridData;
import org.rtspplayer.sample.database.DatabaseHelper;
import org.rtspplayer.sample.dialog.AddChannelDialog;
import org.rtspplayer.sample.dialog.AddChannelDialog.AddChannelDialogListener;
import org.rtspplayer.sample.util.HttpClientFactory;
import org.rtspplayer.sample.util.Logger;
import org.rtspplayer.sample.util.M3U;
import org.rtspplayer.sample.util.OnSwipeTouchListener;
import org.rtspplayer.sample.util.PlayerCallBacks;
import org.rtspplayer.sample.util.SharedSettings;
import org.rtspplayer.sample.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements AddChannelDialogListener, GridAdapterCallback/*, AddChannelDialogListener2*/ {
    public FilesList filesList = null;
    public StreamsList streamsList = null;
    public CamerasList camerasList = null;
    public GridAdapter currentList = null;

    //final static public int MESSAGE_UPDATE_ADAPTER = 1122;

    private DatabaseHelper mDbHelper = null;
    public boolean mCloseIconsIsVisible = false;
    private AddChannelDialog mAddChannelDialog = null;
    //private AddChannelDialog2 mAddChannelDialog2= null;

    public GridData m_cur_item = null;

    final public static String S_CUR_ID = "_id";

    public boolean mPanelIsVisible = true;
    private SystemUiHider hider = null;
    private SystemUiHider hider1 = null;

    static public enum ScreenMode {Preview, OneView, MultiView}

    ;
    public static ScreenMode screenMode = ScreenMode.MultiView;

    static public boolean is_fullScreenMode = false;
    static public boolean isCurItemDir = false;
    boolean checkFrom2x2Mode = false;
    public static boolean fourCamerasGridVisible = false;
    private FrameLayout vSep1 = null;
    private FrameLayout vSep2 = null;
    private FrameLayout hSep = null;
    public static boolean inFileList = false;

    public static int yearSelWidth = 0;
    public boolean isFullScreen = false;
    ListView list_docview = null;

    enum playerFullScreen {
        player1, player2, player3, player4, none
    }

    volatile int currentCameraIn2x2Mode = 0;
    int playersResetIfonBackPressedNumber = 0;

    public static ArrayList<GridData> _2x2camerasData = new ArrayList<GridData>();
    playerFullScreen playerFullScreen;
    LinearLayout playersCon = null;
    private MediaPlayer singleCameraModePlayer = null;
    private MediaPlayer player1 = null;
    private MediaPlayer player2 = null;
    private MediaPlayer player3 = null;
    private MediaPlayer player4 = null;
    private PlayerCallBacks currentPlayerCallBacks = null;
    private PlayerCallBacks singleModePlayerCallBacks = null;

    private PlayerCallBacks Player1CallBacks = null;
    private PlayerCallBacks Player2CallBacks = null;
    private PlayerCallBacks Player3CallBacks = null;
    private PlayerCallBacks Player4CallBacks = null;

    ProgressBar loaderIndicator1 = null;
    ProgressBar loaderIndicator2 = null;
    ProgressBar loaderIndicator3 = null;
    ProgressBar loaderIndicator4 = null;

    private MediaPlayer fullScreenPlayer = null;
    private GridData singlePlayerData = null;
    private LinearLayout playersnames = null;

    //player


    private MediaPlayer player = null;

    private FrameLayout playerContainer = null;
    private ProgressBar progress_bar = null;
    private ImageView picStatusDisconneted = null;
    private ImageView picPreviewLeftTop = null;
    private ImageView picPreviewRightBottom = null;
    private TextView viewListEmpty = null;
    private AudioManager audio_manager = null;

    private RelativeLayout layoutPlayerPanel = null;
    private RelativeLayout layoutPanelPlayerControl = null;
    private TextView textPanelPlayerCaptionText = null;
    private TextView textPanelPlayerCaptionDecoderType = null;
    private TextView textPanelPlayerControlPosition = null;
    private TextView textPanelPlayerControlDuration = null;
    private SeekBar seekPanelPlayerControlSeekbar = null;
    private ImageButton playerPanelControlLock = null;
    private ImageButton playerPanelControlPrevStream = null;
    private ImageButton playerPanelControlPlay = null;
    private ImageButton playerPanelControlNextStream = null;
    private ImageButton playerPanelControlScreen = null;

    //private Timer			playerPanelControlTimer = null;
    private PlayerPanelControlVisibleTask playerPanelControlTask = null;

    public boolean isPlayerPanelVisible = false;

    private FrameLayout layoutPlayerLocked = null;
    private ImageButton playerPanelControlLocked = null;
    public boolean isLocked = false;

    private Drawable[] imgPlayPause = null;
    private Drawable[] imgAspects = null;
    private String densityDpiString = "_mdpi";

    //settings
    //SharedSettings settings = null;
    private boolean firstRunned = true;

    public boolean isStartedByIntent = false;
    public boolean isFileUrl = false;
    private String urlFromIntent = "";

    private boolean isLowResolutionDevice = false;
    private boolean isReturnedFromPreference = false;

//    private final int       waitForHidePlayerPanelControl = 10; // in sec
//    private int 			currentPlayerPanelControlVisibleTime = waitForHidePlayerPanelControl;

    private int previewWidth = 240;
    private int previewHeight = 180;
    private int previewRightMargin = 10;
    private int previewBottomMargin = 10;

    //private int             intPanelPlayerControlPosition  = -1;
    //private int 		    intPanelPlayerControlLastStreamPosition = -1;
    private boolean lockChangePosition = false;
    private int lockedChangePosition = -1;
    private int lockedLatestStreamPosition = -1;

    //actionbar, tabs
    private ActionBar bar = null;
    private Tab tab_files = null;
    private Tab tab_streams = null;
    private Tab tab_cameras = null;
    private TabListener bar_files_tablistener = null;
    private TabListener bar_streams_tablistener = null;
    private TabListener bar_cameras_tablistener = null;

    private boolean showPreview = false;

    //private Menu 	menuMain = null;
    private Object waitOnMe = new Object();


    //check file changes
    boolean is_file_content_dirty = true;

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // do s.th.
            // depending on the handler you might be on the UI
            // thread, so be cautious!
            //uri can be null
            Log.i(TAG, "=onChange register file=" + uri);
            is_file_content_dirty = true;
        }
    }

    private MyContentObserver file_observer_v = null;
    private MyContentObserver file_observer_a = null;


    private boolean isCurrentFilesList() {
        if (currentList == null)
            return false;

        return (currentList.getClass() == FilesList.class);
    }

    private boolean isCurrentStreamsList() {
        if (currentList == null)
            return false;

        return (currentList.getClass() == StreamsList.class);
    }

    private boolean isCurrentCamerasList() {
        if (currentList == null)
            return false;

        return (currentList.getClass() == CamerasList.class);
    }

    private boolean isSameListAsTabId() {
        int idTab = SharedSettings.getInstance().savedTabNumForSavedId;
        return ((idTab == 0 && isCurrentCamerasList()) ||
                (idTab == 1 && isCurrentStreamsList()) ||
                (idTab == 2 && isCurrentFilesList()));
    }

    public String getFileNameByUri(Uri uri, Context context) {
        String fileName = "unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                //fileName = filePathUri.getLastPathSegment().toString();
                // Log.d("1=getFileNameByUri "+filePathUri);
                //fileName = filePathUri.toString()
                fileName = filePathUri.getPath();
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            //fileName = filePathUri.getLastPathSegment().toString();
            //fileName = filePathUri.toString();
            fileName = filePathUri.getPath();
        } else {
            //fileName = fileName+"_"+filePathUri.getLastPathSegment();
            //fileName = filePathUri.toString();
            fileName = filePathUri.getPath();
        }
        return fileName;
    }

    public void setTo2x2CamerasLayout() {


        hider.mAnchorView.setOnSystemUiVisibilityChangeListener(null);

        hider.mAnchorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        player1.toggleMute(true);
        player2.toggleMute(true);
        player3.toggleMute(true);
        player4.toggleMute(true);

        final FrameLayout vSep1 = (FrameLayout) findViewById(R.id.verSeparator1);
        final FrameLayout vSep2 = (FrameLayout) findViewById(R.id.verSeparator2);
        final FrameLayout hSep = (FrameLayout) findViewById(R.id.horSeparator);
        final LinearLayout playerRow0 = (LinearLayout) findViewById(R.id.playerRow0);
        final LinearLayout playerRow1 = (LinearLayout) findViewById(R.id.playerRow1);

        hideProgressView(player);
        int separatorColor = Color.parseColor("#006280");
        playersnames.setVisibility(View.VISIBLE);
        fourCamerasGridVisible = true;

        if (layoutPlayerPanel.getVisibility() == View.VISIBLE) {
            layoutPlayerPanel.setVisibility(View.GONE);

            // dataselector.hideOpts();
        }

        showGridSeparators(separatorColor);

        if (playersResetIfonBackPressedNumber != currentCameraIn2x2Mode)
            switch (playersResetIfonBackPressedNumber) {
                case 0:
                    player1.Close();
                    selectChannel(_2x2camerasData.get(0));
                    break;
                case 1:
                    player2.Close();
                    selectChannel(_2x2camerasData.get(1));
                    break;
                case 2:
                    player3.Close();
                    selectChannel(_2x2camerasData.get(2));
                    break;
                case 3:
                    player4.Close();
                    selectChannel(_2x2camerasData.get(3));
                    break;
            }


        switch (playerFullScreen) {
            case player1:


                LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                parent.height = 0;
                parent.weight = (float) 0.5;
                parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow0.setLayoutParams(parent);

                LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                parent1.height = 0;
                parent1.weight = (float) 0.5;
                parent1.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow1.setLayoutParams(parent1);

                LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player1.getLayoutParams();
                lpc.width = 0;
                lpc.weight = (float) 0.5;
                lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
                player1.setLayoutParams(lpc);

                player1.getSurfaceView().setZOrderOnTop(false);
                player2.getSurfaceView().setZOrderOnTop(false);
                player3.getSurfaceView().setZOrderOnTop(false);
                player4.getSurfaceView().setZOrderOnTop(false);
                playerFullScreen = playerFullScreen.none;
                isFullScreen = false;

                break;
            case player2:


                LinearLayout.LayoutParams parent3 = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                parent3.height = 0;
                parent3.weight = (float) 0.5;
                parent3.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow0.setLayoutParams(parent3);

                LinearLayout.LayoutParams parent4 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                parent4.height = 0;
                parent4.weight = (float) 0.5;
                parent4.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow1.setLayoutParams(parent4);

                LinearLayout.LayoutParams lpc1 = (LinearLayout.LayoutParams) player2.getLayoutParams();
                lpc1.width = 0;
                lpc1.weight = (float) 0.5;
                lpc1.height = LinearLayout.LayoutParams.MATCH_PARENT;
                player2.setLayoutParams(lpc1);

                player1.getSurfaceView().setZOrderOnTop(false);
                player2.getSurfaceView().setZOrderOnTop(false);
                player3.getSurfaceView().setZOrderOnTop(false);
                player4.getSurfaceView().setZOrderOnTop(false);
                isFullScreen = false;
                playerFullScreen = playerFullScreen.none;
                break;
            case player3:


                LinearLayout.LayoutParams parent5 = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                parent5.height = 0;
                parent5.weight = (float) 0.5;
                parent5.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow0.setLayoutParams(parent5);

                LinearLayout.LayoutParams parent6 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                parent6.height = 0;
                parent6.weight = (float) 0.5;
                parent6.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow1.setLayoutParams(parent6);

                LinearLayout.LayoutParams lpc3 = (LinearLayout.LayoutParams) player3.getLayoutParams();

                lpc3.width = 0;
                lpc3.weight = (float) 0.5;
                lpc3.height = LinearLayout.LayoutParams.MATCH_PARENT;
                player3.setLayoutParams(lpc3);

                player1.getSurfaceView().setZOrderOnTop(false);
                player2.getSurfaceView().setZOrderOnTop(false);
                player3.getSurfaceView().setZOrderOnTop(false);
                player4.getSurfaceView().setZOrderOnTop(false);

                isFullScreen = false;
                playerFullScreen = playerFullScreen.none;
                break;
            case player4:

                LinearLayout.LayoutParams parent7 = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                parent7.height = 0;
                parent7.weight = (float) 0.5;
                parent7.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow0.setLayoutParams(parent7);

                LinearLayout.LayoutParams parent8 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                parent8.height = 0;
                parent8.weight = (float) 0.5;
                parent8.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerRow1.setLayoutParams(parent8);


                LinearLayout.LayoutParams lpc4 = (LinearLayout.LayoutParams) player4.getLayoutParams();

                lpc4.width = 0;
                lpc4.weight = (float) 0.5;
                lpc4.height = LinearLayout.LayoutParams.MATCH_PARENT;
                player4.setLayoutParams(lpc4);

                player1.getSurfaceView().setZOrderOnTop(false);
                player2.getSurfaceView().setZOrderOnTop(false);
                player3.getSurfaceView().setZOrderOnTop(false);
                player4.getSurfaceView().setZOrderOnTop(false);

                isFullScreen = false;
                playerFullScreen = playerFullScreen.none;
                break;

        }
    }

    private void set_2x2CamNames(GridData[] gridDatas) {

        if (gridDatas[0] != null)
            ((TextView) findViewById(R.id.camName1)).setText(gridDatas[0].name);

        if (gridDatas[1] != null)
            ((TextView) findViewById(R.id.camName2)).setText(gridDatas[1].name);
        else ((TextView) findViewById(R.id.camName2)).setText("");
        if (gridDatas[2] != null)
            ((TextView) findViewById(R.id.camName3)).setText(gridDatas[2].name);
        else ((TextView) findViewById(R.id.camName3)).setText("");
        if (gridDatas[3] != null)
            ((TextView) findViewById(R.id.camName4)).setText(gridDatas[3].name);
        else ((TextView) findViewById(R.id.camName4)).setText("");
    }

    private void hideGridSeparators() {
        vSep1.setBackgroundColor(Color.BLACK);
        vSep2.setBackgroundColor(Color.BLACK);
        hSep.setBackgroundColor(Color.BLACK);
    }

    private void showGridSeparators(int separatorColor) {

        vSep1.setBackgroundColor(separatorColor);
        vSep2.setBackgroundColor(separatorColor);
        hSep.setBackgroundColor(separatorColor);

    }


    public void start2X2camerasMode(final GridData[] gridDatas) {

        hider.mAnchorView.setOnSystemUiVisibilityChangeListener(null);
        hider.mAnchorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        playerPanelControlPrevStream.setEnabled(false);
        playerPanelControlNextStream.setEnabled(false);

        _2x2camerasData.clear();
        for (GridData item : gridDatas)
            _2x2camerasData.add(item);
        fourCamerasGridVisible = true;

        playersCon.setVisibility(View.VISIBLE);
        playersnames.setVisibility(View.VISIBLE);
        set_2x2CamNames(gridDatas);
        final FrameLayout vSep1 = (FrameLayout) findViewById(R.id.verSeparator1);
        final FrameLayout vSep2 = (FrameLayout) findViewById(R.id.verSeparator2);
        final FrameLayout hSep = (FrameLayout) findViewById(R.id.horSeparator);

        final LinearLayout playerRow0 = (LinearLayout) findViewById(R.id.playerRow0);

        final LinearLayout playerRow1 = (LinearLayout) findViewById(R.id.playerRow1);
        final int separatorColor = Color.parseColor("#006280");

        showGridSeparators(separatorColor);

        player1.setOnTouchListener(new View.OnTouchListener() {
            public boolean statusBarVisible = false;

            public boolean onTouch(View v, MotionEvent event) {
                //if (gridDatas[0].isActive)
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        int y = 0;
                        if (gridDatas[0] != null)
                            if (!isFullScreen) {


                                SharedSettings _set = SharedSettings.getInstance();
                                _set.setLongValueForKey(S_CUR_ID, gridDatas[0].id);
                                _set.savedTabNumForSavedId = _set.selectedTabNum;
                                _set.savePrefSettings();
                                playersResetIfonBackPressedNumber = 0;
                                currentCameraIn2x2Mode = 0;
                                player1.toggleMute(false);
                                player = player1;
                                hider.hide();
                                playersnames.setVisibility(View.INVISIBLE);
                                fourCamerasGridVisible = false;

                                hideGridSeparators();
                                LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                                parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                playerRow0.setLayoutParams(parent);

                                LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                                parent1.height = 0;
                                parent1.width = 0;
                                parent1.weight = (float) 0.0;
                                playerRow1.setLayoutParams(parent1);


                                LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player1.getLayoutParams();

                                lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                player1.setLayoutParams(lpc);

                                player1.getSurfaceView().setZOrderOnTop(false);
                                player2.getSurfaceView().setZOrderOnTop(true);
                                player3.getSurfaceView().setZOrderOnTop(true);
                                player4.getSurfaceView().setZOrderOnTop(true);

                                playerFullScreen = playerFullScreen.player1;
                                isFullScreen = true;

                                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                                hider.setup();
                                hider.mAnchorView.setSystemUiVisibility(uiOptions);


                            } else hider.hide();


                    }
                }

                return true;
            }
        });

        player2.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int y = 0;
                if (gridDatas[1] != null)
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN: {
                            if (!isFullScreen) {

                                SharedSettings _set = SharedSettings.getInstance();
                                _set.setLongValueForKey(S_CUR_ID, gridDatas[1].id);
                                _set.savedTabNumForSavedId = _set.selectedTabNum;
                                _set.savePrefSettings();
                                player2.toggleMute(false);
                                player = player2;
                                playersResetIfonBackPressedNumber = 1;
                                currentCameraIn2x2Mode = 1;


                                hideGridSeparators();
                                playersnames.setVisibility(View.INVISIBLE);
                                fourCamerasGridVisible = false;
                                hider.hide();
                                vSep1.invalidate();
                                hSep.invalidate();
                                LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                                parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                playerRow0.setLayoutParams(parent);

                                LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                                parent1.height = 0;
                                parent1.width = 0;
                                parent1.weight = (float) 0.0;
                                playerRow1.setLayoutParams(parent1);


                                LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player2.getLayoutParams();

                                lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                player2.setLayoutParams(lpc);

                                player1.getSurfaceView().setZOrderOnTop(true);
                                player2.getSurfaceView().setZOrderOnTop(false);
                                player3.getSurfaceView().setZOrderOnTop(true);
                                player4.getSurfaceView().setZOrderOnTop(true);
                                isFullScreen = true;
                                playerFullScreen = playerFullScreen.player2;
                                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                                hider.setup();
                                hider.mAnchorView.setSystemUiVisibility(uiOptions);

                            } else hider.hide();

                        }
                    }

                return true;
            }
        });

        player3.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int y = 0;
                if (gridDatas[2] != null)
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            if (!isFullScreen) {

                                SharedSettings _set = SharedSettings.getInstance();
                                _set.setLongValueForKey(S_CUR_ID, gridDatas[2].id);
                                _set.savedTabNumForSavedId = _set.selectedTabNum;
                                _set.savePrefSettings();
                                player3.toggleMute(false);
                                player = player3;
                                playersResetIfonBackPressedNumber = 2;
                                currentCameraIn2x2Mode = 2;

                                hideGridSeparators();
                                playersnames.setVisibility(View.INVISIBLE);
                                fourCamerasGridVisible = false;
                                hider.hide();


                                LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                                parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                parent.weight = (float) 1.0;
                                playerRow1.setLayoutParams(parent);

                                LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                                parent1.height = 0;
                                parent1.width = 0;
                                parent1.weight = (float) 0.0;
                                playerRow0.setLayoutParams(parent1);


                                LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player3.getLayoutParams();

                                lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                player3.setLayoutParams(lpc);

                                player1.getSurfaceView().setZOrderOnTop(true);
                                player2.getSurfaceView().setZOrderOnTop(true);
                                player3.getSurfaceView().setZOrderOnTop(false);
                                player4.getSurfaceView().setZOrderOnTop(true);

                                isFullScreen = true;
                                playerFullScreen = playerFullScreen.player3;
                                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                                hider.setup();
                                hider.mAnchorView.setSystemUiVisibility(uiOptions);

                            } else hider.hide();

                        }
                    }

                return true;
            }
        });

        player4.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gridDatas[3] != null)
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {

                            if (!isFullScreen) {

                                SharedSettings _set = SharedSettings.getInstance();
                                _set.setLongValueForKey(S_CUR_ID, gridDatas[3].id);
                                _set.savedTabNumForSavedId = _set.selectedTabNum;
                                _set.savePrefSettings();
                                player4.toggleMute(false);
                                player = player4;
                                playersResetIfonBackPressedNumber = 3;
                                currentCameraIn2x2Mode = 3;

                                hideGridSeparators();
                                playersnames.setVisibility(View.INVISIBLE);
                                fourCamerasGridVisible = false;
                                hider.hide();

                                LinearLayout.LayoutParams parent = (LinearLayout.LayoutParams) playerRow1.getLayoutParams();
                                parent.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                parent.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                playerRow1.setLayoutParams(parent);

                                LinearLayout.LayoutParams parent1 = (LinearLayout.LayoutParams) playerRow0.getLayoutParams();
                                parent1.height = 0;
                                parent1.width = 0;
                                parent1.weight = (float) 0.0;
                                playerRow0.setLayoutParams(parent1);

                                LinearLayout.LayoutParams lpc = (LinearLayout.LayoutParams) player4.getLayoutParams();
                                lpc.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                lpc.height = LinearLayout.LayoutParams.MATCH_PARENT;
                                player4.setLayoutParams(lpc);

                                player1.getSurfaceView().setZOrderOnTop(true);
                                player2.getSurfaceView().setZOrderOnTop(true);
                                player3.getSurfaceView().setZOrderOnTop(true);
                                player4.getSurfaceView().setZOrderOnTop(false);

                                isFullScreen = true;
                                playerFullScreen = playerFullScreen.player4;
                                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                                hider.setup();
                                hider.mAnchorView.setSystemUiVisibility(uiOptions);

                            } else hider.hide();

                        }
                    }

                return true;
            }
        });
        player = player1;
        currentPlayerCallBacks = Player1CallBacks;
        selectChannel(gridDatas[0]);
        rtmp:
//10.20.16.128:1936/live/2?sid=lu6hx1dp2sw8xzv6v7c5r31o3c4nqm45

        if (gridDatas[1] != null) {
            player = player2;
            ((TextView) findViewById(R.id.offStatus2)).setVisibility(View.INVISIBLE);
            currentPlayerCallBacks = Player2CallBacks;
            selectChannel(gridDatas[1]);
        } else
            ((TextView) findViewById(R.id.offStatus2)).setVisibility(View.VISIBLE);

        if (gridDatas[2] != null) {
            player = player3;
            ((TextView) findViewById(R.id.offStatus3)).setVisibility(View.INVISIBLE);
            currentPlayerCallBacks = Player3CallBacks;
            selectChannel(gridDatas[2]);
        } else
            ((TextView) findViewById(R.id.offStatus3)).setVisibility(View.VISIBLE);

        if (gridDatas[3] != null) {
            player = player4;
            ((TextView) findViewById(R.id.offStatus4)).setVisibility(View.INVISIBLE);
            currentPlayerCallBacks = Player4CallBacks;
            selectChannel(gridDatas[3]);
        } else
            ((TextView) findViewById(R.id.offStatus4)).setVisibility(View.VISIBLE);

        player1.toggleMute(true);
        player2.toggleMute(true);
        player3.toggleMute(true);
        player4.toggleMute(true);
        // player.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
//    ViewTreeObserver.OnGlobalLayoutListener layoutListListener = new ViewTreeObserver.OnGlobalLayoutListener()
//    {
//        @Override
//        public void onGlobalLayout()
//        {
//        	if (getActionBar() == null)
//        		return;
//
//            int barHeight = getActionBar().getHeight();
//            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) list_docview.getLayoutParams();
//            // The list view top-margin should always match the action bar height
//            if (params.topMargin != barHeight)
//            {
//                params.topMargin = barHeight;
//                list_docview.setLayoutParams(params);
//            }
//            // The action bar doesn't update its height when hidden, so make top-margin zero
//            if (!getActionBar().isShowing())
//            {
//            	params.topMargin = 0;
//            	list_docview.setLayoutParams(params);
//            }
//        }
//    };
//
//    ViewTreeObserver.OnGlobalLayoutListener layoutEmptyListener = new ViewTreeObserver.OnGlobalLayoutListener()
//    {
//        @Override
//        public void onGlobalLayout()
//        {
//        	if (getActionBar() == null)
//        		return;
//
//            int barHeight = getActionBar().getHeight();
//            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewListEmpty.getLayoutParams();
//            // The list view top-margin should always match the action bar height
//            if (params.topMargin != barHeight)
//            {
//                params.topMargin = barHeight;
//                viewListEmpty.setLayoutParams(params);
//            }
//            // The action bar doesn't update its height when hidden, so make top-margin zero
//            if (!getActionBar().isShowing())
//            {
//            	params.topMargin = 0;
//            	viewListEmpty.setLayoutParams(params);
//            }
//        }
//    };

    ViewTreeObserver.OnGlobalLayoutListener layoutListListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (player != null) {
                int color = ((ColorDrawable) player.getBackground()).getColor();
                if (isPlayerStarted() && color != Color.BLACK) {
                    player.backgroundColor(Color.BLACK);
                } else if (!isPlayerStarted() && color != Color.parseColor("#DDDDDD") && mPanelIsVisible) {
                    player.backgroundColor(Color.parseColor("#DDDDDD"));
                }
            }

            if (getActionBar() == null)
                return;

            int barHeight = getActionBar().getHeight();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) list_docview.getLayoutParams();
            // The list view top-margin should always match the action bar height
            if (getActionBar().isShowing() && params.topMargin != barHeight) {
                params.topMargin = barHeight;
                list_docview.setLayoutParams(params);
            }
            // The action bar doesn't update its height when hidden, so make top-margin zero
            if (!getActionBar().isShowing() && params.topMargin != 0) {
                params.topMargin = 0;
                list_docview.setLayoutParams(params);
            }
        }
    };

    ViewTreeObserver.OnGlobalLayoutListener layoutEmptyListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (getActionBar() == null)
                return;

            int barHeight = getActionBar().getHeight();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewListEmpty.getLayoutParams();
            float incf = 20f;
            // The list view top-margin should always match the action bar height
            if (getActionBar().isShowing() && params.topMargin != (barHeight + (int) pxFromDp(incf))) {
                params.topMargin = barHeight + (int) pxFromDp(incf);
                viewListEmpty.setLayoutParams(params);
            }
            // The action bar doesn't update its height when hidden, so make top-margin zero
            if (!getActionBar().isShowing() && params.topMargin != (int) pxFromDp(incf)) {
                params.topMargin = (int) pxFromDp(incf);
                viewListEmpty.setLayoutParams(params);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        Log.v(TAG, "=>onCreate");

        SharedSettings.getInstance(this).loadPrefSettings();
        SharedSettings.getInstance().savePrefSettings();
        int currentMode = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("view", -1);

        switch (currentMode) {
            case (0):
                screenMode = ScreenMode.Preview;
                break;
            case (1):
                screenMode = ScreenMode.OneView;
                break;
            case (2):
                screenMode = ScreenMode.MultiView;
                break;
            default:
                screenMode = ScreenMode.MultiView;
        }


        showPreview = guardedByBuildversionBooleanValue(SharedSettings.getInstance().showPreview);

        printResolutionDevice();

        audio_manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        progress_bar = (ProgressBar) findViewById(R.id.layout_hide_progress);
        playerContainer = (FrameLayout) findViewById(R.id.player_container);
        picStatusDisconneted = (ImageView) findViewById(R.id.pic_status_disconnected);
        picPreviewLeftTop = (ImageView) findViewById(R.id.pic_preview_lefttop);
        picPreviewRightBottom = (ImageView) findViewById(R.id.pic_preview_rightbottom);
        viewListEmpty = (TextView) findViewById(R.id.textListEmpty);

        layoutPlayerLocked = (FrameLayout) findViewById(R.id.layoutPlayerLocked);
        playerPanelControlLocked = (ImageButton) findViewById(R.id.buttonPanelPlayerControlLocked);

        layoutPlayerPanel = (RelativeLayout) findViewById(R.id.layoutPlayerPanel);
        layoutPanelPlayerControl = (RelativeLayout) findViewById(R.id.layoutPanelPlayerControl);

        textPanelPlayerCaptionText = (TextView) findViewById(R.id.textPanelPlayerCaptionText);
        textPanelPlayerCaptionDecoderType = (TextView) findViewById(R.id.textPanelPlayerCaptionDecoderType);
        textPanelPlayerControlPosition = (TextView) findViewById(R.id.textPanelPlayerControlPosition);
        textPanelPlayerControlDuration = (TextView) findViewById(R.id.textPanelPlayerControlDuration);

        playerPanelControlLock = (ImageButton) findViewById(R.id.buttonPanelPlayerControlLock);
        playerPanelControlPrevStream = (ImageButton) findViewById(R.id.buttonPanelPlayerControlPrevious);
        playerPanelControlPlay = (ImageButton) findViewById(R.id.buttonPanelPlayerControlPlayPause);
        playerPanelControlNextStream = (ImageButton) findViewById(R.id.buttonPanelPlayerControlNext);
        playerPanelControlScreen = (ImageButton) findViewById(R.id.buttonPanelPlayerControlAspect);

        list_docview = (ListView) findViewById(R.id.listDocuments);
        player1 = (MediaPlayer) findViewById(R.id.playerView1);

        playerContainer.setVisibility(screenMode == ScreenMode.Preview ? View.VISIBLE : View.INVISIBLE);

        player = player1;
        player2 = (MediaPlayer) findViewById(R.id.playerView2);

        player3 = (MediaPlayer) findViewById(R.id.playerView3);
        player4 = (MediaPlayer) findViewById(R.id.playerView4);
        singleCameraModePlayer = (MediaPlayer) findViewById(R.id.playerView);

        singleModePlayerCallBacks = new PlayerCallBacks(this, singleCameraModePlayer);
        currentPlayerCallBacks = singleModePlayerCallBacks;
        Player1CallBacks = new PlayerCallBacks(this, player1);
        Player2CallBacks = new PlayerCallBacks(this, player2);
        Player3CallBacks = new PlayerCallBacks(this, player3);
        Player4CallBacks = new PlayerCallBacks(this, player4);

        playersnames = (LinearLayout) findViewById(R.id.playersNames);
        playersCon = (LinearLayout) findViewById(R.id.players2x2_container);
        loaderIndicator1 = (ProgressBar) findViewById(R.id.loaderIndicator1);
        loaderIndicator2 = (ProgressBar) findViewById(R.id.loaderIndicator2);
        loaderIndicator3 = (ProgressBar) findViewById(R.id.loaderIndicator3);
        loaderIndicator4 = (ProgressBar) findViewById(R.id.loaderIndicator4);
        vSep1 = (FrameLayout) findViewById(R.id.verSeparator1);
        vSep2 = (FrameLayout) findViewById(R.id.verSeparator2);
        hSep = (FrameLayout) findViewById(R.id.horSeparator);
        ViewTreeObserver observerList = list_docview.getViewTreeObserver();
        observerList.addOnGlobalLayoutListener(layoutListListener);

        ViewTreeObserver observerEmpty = viewListEmpty.getViewTreeObserver();
        observerEmpty.addOnGlobalLayoutListener(layoutEmptyListener);

        hider = SystemUiHider.getInstance(this, findViewById(R.id.container), SystemUiHider.FLAG_HIDE_NAVIGATION);
        hider.setup();
        hider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void onVisibilityChange(boolean visible) {
                if (isLocked) {
                    hider.hide();
                    return;
                }

                if (visible && !mPanelIsVisible) {
                    updatePlayerPanelControl(true, isLocked);
                }
                if (!visible) {
                    updatePlayerPanelControl(false, isLocked);
                }
            }
        });


        firstRunned = true;

        mDbHelper = new DatabaseHelper(this);

        filesList = new FilesList(this, mDbHelper, this);
        is_file_content_dirty = true;

        streamsList = new StreamsList(this, mDbHelper, this);
        camerasList = new CamerasList(this, this, mDbHelper, this);

        //mAddChannelDialog2 = new AddChannelDialog2(this, true);
        //mAddChannelDialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mAddChannelDialog = new AddChannelDialog(this, Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
        mCloseIconsIsVisible = false;


        //player.getThumbnailFromStream("Test url");

        // for proper scaling
        picStatusDisconneted.getDrawable().setLevel(5000);
        picPreviewLeftTop.getDrawable().setLevel(5000);
        picPreviewRightBottom.getDrawable().setLevel(5000);
        playerPanelControlLock.getDrawable().setLevel(5000);
        playerPanelControlPrevStream.getDrawable().setLevel(5000);
        playerPanelControlPlay.getDrawable().setLevel(5000);
        playerPanelControlNextStream.getDrawable().setLevel(5000);
        playerPanelControlScreen.getDrawable().setLevel(5000);
        playerPanelControlLocked.getDrawable().setLevel(5000);

        picPreviewRightBottom.setVisibility(View.GONE);

        seekPanelPlayerControlSeekbar = (SeekBar) findViewById(R.id.seekPanelPlayerControlSeekbar);
        seekPanelPlayerControlSeekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                refreshPlayerPanelControlVisibleTimer();
                if (player == null)
                    return true;

                Position position = player.getLiveStreamPosition();
                if (position == null)
                    return true;

                if (position.getStreamType() != 2) {
                    long pos = position.getDuration() - 1000;
                    if (pos <= 0 || (pos > (60 * 60 * 12 * 1000))) {
                        return true;
                    }
                }

                return false;
            }
        });

        seekPanelPlayerControlSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (player == null || !arg2)
                    return;

                Position position = player.getLiveStreamPosition();
                if (position == null)
                    return;

                if (position.getStreamType() != 2) {
                    long pos = position.getDuration() - 1000;
                    if (pos > 0 && (pos <= (60 * 60 * 12 * 1000))) {
                        player.setStreamPosition(arg1);
                        refreshPlayerPanelControlVisibleTimer();
                    }
                    return;
                }

                lockChangePosition = true;
                lockedChangePosition = arg1;

                player.setLiveStreamPosition(position.getFirst() + lockedChangePosition);

                Log.i(TAG, "mediaLivePositionUpdate from onProgressChanged.");
                mediaLivePositionUpdate();
                //player.setStreamPosition(arg1);
                refreshPlayerPanelControlVisibleTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                refreshPlayerPanelControlVisibleTimer();

            }
        });

        list_docview.setAdapter(currentList);


        //init database
        updatePlayerPanelControl(false, isLocked);

        //isLowResolutionDevice = isLowResolutionDevice();


        //onInit();
        list_docview.setOnTouchListener(new OnSwipeTouchListener(false) {
            public void touch() {
                Log.i(TAG, "=OnSwipeTouchListener touch");
            }

            public void swipeRight(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener swipeRight");
                if (isLocked)
                    return;

                int sel_channel = list_docview.pointToPosition(x, y);//chs_list.GetSelectedChannel();
                if (sel_channel == AdapterView.INVALID_POSITION)
                    return;

                GridData gd = null;
                try {
                    gd = (GridData) list_docview.getItemAtPosition(sel_channel);
                } catch (IndexOutOfBoundsException e) {
                }

                if (gd == null)
                    return;

                if (isModeFile()) {
                    //selectChannel(gd);
                } else
                    onDeleteChannel(gd);
            }

            public void swipeLeft(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener swipeLeft");
                if (isLocked)
                    return;

                int sel_channel = list_docview.pointToPosition(x, y);//chs_list.GetSelectedChannel();
                if (sel_channel == AdapterView.INVALID_POSITION)
                    return;

                GridData gd = null;
                try {
                    gd = (GridData) list_docview.getItemAtPosition(sel_channel);
                } catch (IndexOutOfBoundsException e) {
                }

                if (gd == null)
                    return;

                if (isModeFile()) {
                    //selectChannel(gd);
                } else
                    onDeleteChannel(gd);
            }

            public void swipeTop() {
                Log.i(TAG, "=OnSwipeTouchListener swipeTop");
            }

            public void swipeBottom() {
                Log.i(TAG, "=OnSwipeTouchListener swipeBottom");
            }

            public void doubleTap(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener doubleTap x=" + x + " y=" + y);
                if (isLocked)
                    return;

                if (isModeFile()) {
                    selectChannel(x, y);
                } else
                    onChannelEdit(x, y);
            }

            public void longPress(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener longPress x=" + x + " y=" + y);
                if (isLocked)
                    return;

                if (isModeFile()) {
                    selectChannel(x, y);
                } else
                    onChannelEdit(x, y);
            }

            public void singleTap(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener singleTap x=" + x + " y=" + y);
                if (isLocked)
                    return;
                if (screenMode == ScreenMode.MultiView) {
                    boolean start2x2CamGrid = false;
                    if (currentList != filesList)
                        start2x2CamGrid = true;
                    else {
                        checkFrom2x2Mode = true;
                        selectChannel(x, y);
                        if (!isCurItemDir)
                            start2x2CamGrid = true;
                        else
                            start2x2CamGrid = false;
                    }
                    checkFrom2x2Mode = false;
                    if (start2x2CamGrid) {


                        int i = list_docview.pointToPosition(x, y);

                        if (currentList.checkedCams.size() == 0) {
                            Toast.makeText(MainActivity.this,getResources().getString(R.string.select_at_one_item), Toast.LENGTH_LONG).show();
                        } else {
                            hideControlPanelAndGrid();
                            GridData[] arr = new GridData[4];
                            int size = currentList.checkedCams.size();
                            int dif = 4 - currentList.checkedCams.size();
                            int level = 0;
                            if (size >= 4)
                                level = 4;
                            else level = size;
                            for (int u = 0; u < level; u++)
                                arr[u] = currentList.checkedCams.get(u);
                            if (size < 4)
                                for (int k = size; k < 4; ++k)
                                    arr[k] = null;
                            start2X2camerasMode(arr);
                        }

                    }


                } else {


                    player = singleCameraModePlayer;
                    currentPlayerCallBacks = singleModePlayerCallBacks;


                    if (screenMode == ScreenMode.Preview) {
                        selectChannel(x, y);
                    } else {
                        boolean startFullScreen = false;
                        if (currentList != filesList)
                            startFullScreen = true;
                        else {
                            checkFrom2x2Mode = true;
                            selectChannel(x, y);
                            if (!isCurItemDir)
                                startFullScreen = true;
                            else
                                startFullScreen = false;
                        }
                        checkFrom2x2Mode = false;
                        if (startFullScreen) {
                            playerContainer.setVisibility(View.VISIBLE);
                            hideControlPanelAndGrid();
                            mCloseIconsIsVisible = false;
                            currentList.notifyDataSetChanged();
                            selectChannel(x, y);

                        }
                    }

                }
            }

        });

        layoutPanelPlayerControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                refreshPlayerPanelControlVisibleTimer();
                return true;
            }
        });
        player = singleCameraModePlayer;

        hideProgressView(player);

        player.setOnTouchListener(new OnSwipeTouchListener() {
            public void swipeRight(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener swipeRight");
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();

                if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                    return;

                if (mPanelIsVisible || isStartedByIntent)
                    return;

                selectPreviousChannel();
            }

            public void swipeLeft(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener swipeLeft");
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();
                if (mPanelIsVisible && !isStartedByIntent && showPreview) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setMessage(getResources().getString(R.string.dialog_hide_preview_video));

                    alertDialog.setPositiveButton(getResources().getString(R.string.dialog_exit_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showPreview = false;
                            hidePlayerView();
                            if (player != null)
                                player.Close();
                        }
                    });

                    alertDialog.setNegativeButton(getResources().getString(R.string.dialog_exit_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    alertDialog.show();
                }

                if (mPanelIsVisible || isStartedByIntent)
                    return;

                if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                    return;

                selectNextChannel();
            }

            public void scrollUp() {
                Log.i(TAG, "=OnSwipeTouchListener scrollUp");
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();

                if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                    return;

                if (mPanelIsVisible)
                    return;

                audio_manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }

            public void scrollDown() {
                Log.i(TAG, "=OnSwipeTouchListener scrollDown");
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();

                if (SharedSettings.getInstance().rendererAspectRatioMode == 4)
                    return;

                if (mPanelIsVisible)
                    return;

                audio_manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }

            public void singleTap(int x, int y) {
                Log.i(TAG, "=OnSwipeTouchListener singleTap " + player.getState());
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();
                if (isStartedByIntent) {
                    updatePlayerPanelControl(!isPlayerPanelVisible, isLocked);
                    return;
                }

                if (mPanelIsVisible && player != null && player.getState() == PlayerState.Closed) {
                    return;
                }

                if (mPanelIsVisible) {
                    playerPanelControlPrevStream.setEnabled(true);
                    playerPanelControlNextStream.setEnabled(true);
                    hideControlPanelAndGrid();
                    mCloseIconsIsVisible = false;
                    currentList.notifyDataSetChanged();
                } else {
                    updatePlayerPanelControl(!isPlayerPanelVisible, isLocked);
                    if (SharedSettings.getInstance().AllowFullscreenMode)
                        hider.hide();
                }
            }

            long t = 0;
            int Growing = 0;

            public void pinchMove(boolean isGrow) {
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();

                if (SharedSettings.getInstance().rendererAspectRatioMode != 4)
                    return;

                Log.i(TAG, "=OnSwipeTouchListener pinchMove  isGrow:" + isGrow);


                if (isGrow) {
                    Growing++;
                    if (Growing >= 5) Growing = 5;
                } else {
                    Growing--;
                    if (Growing <= -5) Growing = -5;
                }

                Log.i(TAG, "=OnSwipeTouchListener pinchMove Growing:" + Growing + "  t:" + t + " t_diff:" + (System.nanoTime() - t));

                if (t != 0 && (System.nanoTime() - t) > /*1000000000*/10000000 /* 10 milliseconds */) {
                    if (Growing >= 0) {
                        if ((SharedSettings.getInstance().rendererAspectRatioZoomModePercent + 2) <= 300)
                            SharedSettings.getInstance().rendererAspectRatioZoomModePercent += 2;
                        else
                            SharedSettings.getInstance().rendererAspectRatioZoomModePercent = 300;

                    } else {
                        if ((SharedSettings.getInstance().rendererAspectRatioZoomModePercent - 2) >= 25)
                            SharedSettings.getInstance().rendererAspectRatioZoomModePercent -= 2;
                        else
                            SharedSettings.getInstance().rendererAspectRatioZoomModePercent = 25;
                    }
//						SharedSettings.getInstance().rendererAspectRatioMoveModeX = -1;
//						SharedSettings.getInstance().rendererAspectRatioMoveModeY = -1;
                    player.getConfig().setAspectRatioMoveModeX(SharedSettings.getInstance().rendererAspectRatioMoveModeX);
                    player.getConfig().setAspectRatioMoveModeY(SharedSettings.getInstance().rendererAspectRatioMoveModeY);
                    player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    player.getConfig().setAspectRatioMode(SharedSettings.getInstance().rendererAspectRatioMode);
                    player.UpdateView();
                    Log.i(TAG, "=OnSwipeTouchListener pinchMove ZoomModePercent:" + SharedSettings.getInstance().rendererAspectRatioZoomModePercent + "  t:" + t + " t_diff:" + (System.nanoTime() - t));
                    t = System.nanoTime();
                } else if (t == 0) {
                    t = System.nanoTime();
                }

            }

            long t1 = 0;
            int last_x = -1;
            int last_y = -1;
            int diff_x = -1;
            int diff_y = -1;
            int max_x = -1;
            int max_y = -1;
            int _x = 50;    // Center
            int _y = 50; // Center
            int _x_last = -1;
            int _y_last = -1;


            public void touchDown(int x, int y) {
                // Reset parameters
                last_x = -1;
                last_y = -1;
                diff_x = -1;
                diff_y = -1;
                _x_last = -1;
                _y_last = -1;

            }

            public void touchMove(int x, int y) {
                if (isLocked)
                    return;

                refreshPlayerPanelControlVisibleTimer();

                if (SharedSettings.getInstance().rendererAspectRatioMode != 4)
                    return;

                if (-1 == max_x || -1 == max_y) {
                    Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    max_x = d.getWidth();
                    max_y = d.getHeight();
                }

                Log.i(TAG, "=OnSwipeTouchListener touchMove x:" + x + " y:" + y + " t:" + t1 + " t_diff:" + (System.nanoTime() - t1));

                if (true /*t1 != 0 && (System.nanoTime() - t) > /*1000000000*10000000 /* 10 milliseconds */) {
                    if (last_x != -1 && Math.abs(last_x - x) < (max_x / 10)) {
                        diff_x = last_x - x;
                        _x += diff_x * 100 / (max_x / 1.5);

                        if (_x > 100) _x = 100;
                        if (_x < 0) _x = 0;
                    }
                    last_x = x;

                    if (last_y != -1 && Math.abs(last_y - y) < (max_y / 10)) {
                        diff_y = last_y - y;
                        _y -= diff_y * 100 / (max_y / 2.5);
                        if (_y > 100) _y = 100;
                        if (_y < 0) _y = 0;
                    }
                    last_y = y;

                    Log.i(TAG, "=OnSwipeTouchListener touchMove1 _x:" + _x + " diff_x:" + diff_x + " last_x:" + last_x + " max_x:" + max_x);
                    Log.i(TAG, "=OnSwipeTouchListener touchMove1 _y:" + _y + " diff_y:" + diff_y + " last_y:" + last_y + " max_y:" + max_y);


                    if (_x_last != _x || _y_last != _y) {
                        SharedSettings.getInstance().rendererAspectRatioMoveModeX = _x;
                        SharedSettings.getInstance().rendererAspectRatioMoveModeY = _y;
                        player.getConfig().setAspectRatioMoveModeX(SharedSettings.getInstance().rendererAspectRatioMoveModeX);
                        player.getConfig().setAspectRatioMoveModeY(SharedSettings.getInstance().rendererAspectRatioMoveModeY);
                        player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                        player.getConfig().setAspectRatioMode(5);
                        player.UpdateView();
                        t1 = System.nanoTime();
                    }

                    _x_last = _x;
                    _y_last = _y;
                } else if (t1 == 0) {
                    t1 = System.nanoTime();
                }
            }
        });

        int selectedTab = SharedSettings.getInstance().selectedTabNum;

        bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setTitle(getResources().getString(R.string.app_name));

        bar_files_tablistener = new TabListener() {
            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                Log.v(TAG, "=onTabSelected2 tab=" + tab.getText());
                inFileList = true;
                if (list_docview == null)
                    return;

                SharedSettings.getInstance().selectedTabNum = 2;
                SharedSettings.getInstance().savePrefSettings();

                if (currentList != null)
                    currentList.Close();

                currentList = filesList;
                list_docview.setAdapter(currentList);
                Log.v(TAG, "=onTabSelected2 tab=" + is_file_content_dirty);
                is_file_content_dirty = true;
                if (is_file_content_dirty) {
                    currentList.Refresh();
                    is_file_content_dirty = false;
                }
                refreshListEmptyState();
                invalidateOptionsMenu();

//				Log.v(TAG, "=onTabSelected tab=playerConnectinFullScreen");
//				isStartedByIntent = true;
//				playerConnectinFullScreen();
            }

            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {
            }
        };

        bar_streams_tablistener = new TabListener() {
            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                Log.v(TAG, "=onTabSelected3 tab=" + tab.getText());
                inFileList = false;
                if (list_docview == null)
                    return;

                SharedSettings.getInstance().selectedTabNum = 1;
                SharedSettings.getInstance().savePrefSettings();

                if (currentList != null)
                    currentList.Close();

                currentList = streamsList;
                list_docview.setAdapter(currentList);
                currentList.Refresh();
                refreshListEmptyState();
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {
            }
        };

        bar_cameras_tablistener = new TabListener() {
            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                Log.v(TAG, "=onTabSelected4 tab=" + tab.getText());
                inFileList = false;
                if (list_docview == null)
                    return;

                SharedSettings.getInstance().selectedTabNum = 0;
                SharedSettings.getInstance().savePrefSettings();

                if (currentList != null)
                    currentList.Close();

                currentList = camerasList;
                list_docview.setAdapter(currentList);
                currentList.Refresh();
                refreshListEmptyState();
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {
            }
        };

        tab_files = bar.newTab();
        tab_streams = bar.newTab();
        tab_cameras = bar.newTab();
        bar.addTab(tab_cameras.setText(getResources().getString(R.string.tab_name_cameras)).setTabListener(bar_cameras_tablistener), false);
        bar.addTab(tab_streams.setText(getResources().getString(R.string.tab_name_streams)).setTabListener(bar_streams_tablistener), false);
        bar.addTab(tab_files.setText(getResources().getString(R.string.tab_name_files)).setTabListener(bar_files_tablistener), false);

        Log.v(TAG, "Selected tab on start: " + selectedTab);
        if (selectedTab == 0)
            bar.selectTab(tab_cameras);
        if (selectedTab == 1)
            bar.selectTab(tab_streams);
        if (selectedTab == 2)
            bar.selectTab(tab_files);

        String mLocation = null;

        long intentPosition = -1; // position passed in by intent (ms)

        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            /* Started from external application */
            if (getIntent().getData() != null
                    && getIntent().getData().getScheme() != null
                    && getIntent().getData().getScheme().equals("content")) {
                if (getIntent().getData().getHost().equals("media") || getIntent().getData().getHost().equals("mms")) {
                    // Media URI
                    //Cursor cursor = managedQuery(getIntent().getData(), new String[]{ MediaStore.Video.Media.DATA }, null, null, null);
                    //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
//                    if (cursor.moveToFirst())
//                        mLocation = LibVLC.PathToURI(cursor.getString(column_index));
//					 Log.e(TAG, "mms and media");
                } else if (getIntent().getData().getHost().equals("com.fsck.k9.attachmentprovider")
                        || getIntent().getData().getHost().equals("gmail-ls")) {
                    // Mail-based apps - download the stream to a temporary file and play it
                    Log.e(TAG, "URL: gmail-ls");
                    try {
                        /*
                        Cursor cursor = getContentResolver().query(getIntent().getData(), new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        cursor.moveToFirst();
                        String filename = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                        Log.i(TAG, "Getting file " + filename + " from content:// URI");
                        InputStream is = getContentResolver().openInputStream(getIntent().getData());
                        OutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Download/" + filename);
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while((bytesRead = is.read(buffer)) >= 0) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.close();
                        is.close();
                        mLocation = LibVLC.PathToURI(Environment.getExternalStorageDirectory().getPath() + "/Download/" + filename);
                        */
                    } catch (Exception e) {
                        Log.e(TAG, "URL: Couldn't download file from mail URI");
                        //encounteredError();
                    }
                } else {
                    // other content-based URI (probably file pickers)
                    Log.e(TAG, "URL: other content-based URI");
                    mLocation = getIntent().getData().getPath();
                }
            } else if (getIntent().getDataString() != null) {
                // Plain URI
                Log.e(TAG, "URL: Plain URI");
                mLocation = getIntent().getDataString();
                // Remove VLC prefix if needed
                if (mLocation.startsWith("vlc://")) {
                    mLocation = mLocation.substring(6);
                }
                // Decode URI
                //if (!mLocation.contains("/"))
                {
                    try {
                        mLocation = URLDecoder.decode(mLocation, "UTF-8");
                        Log.e(TAG, "URL: Decode URI");
                    } catch (UnsupportedEncodingException e) {
                        //e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        //e.printStackTrace();
                    }
                }
            } else {
                Log.e(TAG, "URL: Couldn't understand the intent");
                //encounteredError();
            }
            if (getIntent().getExtras() != null) {
                intentPosition = getIntent().getExtras().getLong("position", -1);
                Log.e(TAG, "URL: Position: " + intentPosition);
            }
        }

        Log.e(TAG, "URL: " + mLocation);

        if (mLocation != null && mLocation.toUpperCase().indexOf(".M3U") != -1 && mLocation.toUpperCase().indexOf(".M3U8") == -1) {
            Log.w(TAG, "URL1: " + mLocation + "/" + getIntent().getData());
            //File file = new File(mLocation);
            if (isUrlFile(mLocation) == true)
                mLocation = getFileNameByUri(getIntent().getData(), getApplicationContext());
            Log.w(TAG, "URL2: " + mLocation);
            addM3UToList(mLocation);
            bar.selectTab(tab_streams);
        }
        // check action here
        else if (mLocation != null && !mLocation.isEmpty()) {
            isStartedByIntent = true;
            urlFromIntent = mLocation;

            if (mLocation.contains("content://")) {
                urlFromIntent = getRealPathFromURI(Uri.parse(mLocation));
                Log.v(TAG, "=>onCreate converted url: " + urlFromIntent);
            }

            isFileUrl = isUrlFile(urlFromIntent);
            playerConnect(urlFromIntent);
        }

        playerPanelControlLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenMode == ScreenMode.MultiView)
                    switch (playerFullScreen) {
                        case player1:
                            player = player1;
                            break;
                        case player2:
                            player = player2;
                            break;
                        case player3:
                            player = player3;
                            break;
                        case player4:
                            player = player4;
                            break;

                    }
                else
                    player = singleCameraModePlayer;

                refreshPlayerPanelControlVisibleTimer();

                isLocked = true;
                updatePlayerPanelControl(!isLocked, isLocked);

                if (SharedSettings.getInstance().AllowFullscreenMode)
                    hider.hide();

            }
        });

        playerPanelControlLocked.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                refreshPlayerPanelControlVisibleTimer();

                isLocked = false;
                updatePlayerPanelControl(!isLocked, isLocked);

            }
        });

        playerPanelControlPrevStream.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                refreshPlayerPanelControlVisibleTimer();
                //selectNextChannel();
                playNextChannelOrBack();
                //Position position = player.getLiveStreamPosition();
                //player.setLiveStreamPosition(position - 10000);
                //player.setLiveStreamPosition(0x7FFFFFFF);
                //player.Play();
            }
        });

        playerPanelControlPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPlayerPanelControlVisibleTimer();

                if (player == null || (player.getState() != PlayerState.Started && player.getState() != PlayerState.Paused))
                    return;

                switch (player.getState()) {
                    case Started:
                        player.Pause();
                        updatePlayerPanelControlButtons(isLocked, false, SharedSettings.getInstance().rendererAspectRatioMode);
                        break;

                    case Paused:
                        player.Play();
                        updatePlayerPanelControlButtons(isLocked, true, SharedSettings.getInstance().rendererAspectRatioMode);
                        break;

                    default:
                        break;
                }
            }
        });

        playerPanelControlNextStream.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                refreshPlayerPanelControlVisibleTimer();
                //selectPreviousChannel();
                playPreviousChannelOrBack();
                //Position position = player.getLiveStreamPosition();
                //player.setLiveStreamPosition(position + 10000);
                //player.setLiveStreamPosition(0x0);
            }
        });

        playerPanelControlScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPlayerPanelControlVisibleTimer();
                if (player == null)
                    return;

                SharedSettings sett = SharedSettings.getInstance();

                sett.rendererAspectRatioMode++;
                if (sett.rendererAspectRatioMode > 4)
                    sett.rendererAspectRatioMode = 0;

                SharedSettings.getInstance().savePrefSettings();

                //player.UpdateView(guardedByOrientationIntValue(sett.rendererEnableAspectRatio) == 1);
                if (screenMode != ScreenMode.MultiView) {
                    player.getConfig().setAspectRatioMode(sett.rendererAspectRatioMode);
                    player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    player.UpdateView();
                }
                {
                    player1.getConfig().setAspectRatioMode(sett.rendererAspectRatioMode);
                    player1.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    player1.UpdateView();

                    player2.getConfig().setAspectRatioMode(sett.rendererAspectRatioMode);
                    player2.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    player2.UpdateView();

                    player3.getConfig().setAspectRatioMode(sett.rendererAspectRatioMode);
                    player3.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    player3.UpdateView();

                    player4.getConfig().setAspectRatioMode(sett.rendererAspectRatioMode);
                    player4.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
                    player4.UpdateView();
                }


                updatePlayerPanelControlButtons(isLocked, (player.getState() == PlayerState.Started), sett.rendererAspectRatioMode);

            }
        });
    }

    public boolean isModeFile() {
        return isCurrentFilesList();
    }

    public boolean isModeCameras() {
        return isCurrentCamerasList();
    }

    public boolean isPlayerBusy() {
        if (player != null && (player.getState() == PlayerState.Closing ||
                player.getState() == PlayerState.Opening)) {
            return true;
        }
        return false;
    }

    public boolean isPlayerStarted() {
        PlayerState sstate = (player == null) ? PlayerState.Closed : player.getState();
        if (sstate == PlayerState.Opened ||
                sstate == PlayerState.Opening ||
                sstate == PlayerState.Paused ||
                sstate == PlayerState.Started) {
            return true;
        } else {
            return false;
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private boolean isUrlFile(String url) {
        return (url != null && !url.isEmpty() &&
                (!url.contains("://") || url.contains("file://")));
    }

    private void showExitDialog() {
        if (isStartedByIntent) {
            finish();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.dialog_exit_message));

        alertDialog.setPositiveButton(getResources().getString(R.string.dialog_exit_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.dialog_exit_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
        return;
    }

    public void selectCurrent() {
        SharedSettings _set = SharedSettings.getInstance();
        long id = _set.getLongValueForKey(S_CUR_ID);
        selectChannel(currentList.getItem(id));
    }

    public void selectChannel(int x, int y) {
        int sel_channel = list_docview.pointToPosition(x, y);
        if (sel_channel == AdapterView.INVALID_POSITION)
            return;

        if (mAddChannelDialog.isShowing() && !isStartedByIntent)
            return;

        try {
            GridData gd = (GridData) list_docview.getItemAtPosition(sel_channel);
            selectChannel(gd);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void selectChannel(GridData gd) {
        if (gd == null)
            return;

        Log.v(TAG, "=selectChannel gd=" + gd.url);
        if (!currentList.SelectItem(gd)) {
            isCurItemDir = true;
            return;
        } else if (checkFrom2x2Mode) {
            isCurItemDir = false;
            return;
        }


        SharedSettings _set = SharedSettings.getInstance();
        _set.setLongValueForKey(S_CUR_ID, gd.id);
        _set.savedTabNumForSavedId = _set.selectedTabNum;
        _set.savePrefSettings();

        playerConnect(gd);
    }

    private void selectNextChannel() {
        if (isModeFile()) {
            selectNextFile();
            return;
        }

        SharedSettings _set = SharedSettings.getInstance();
        long id = _set.getLongValueForKey(S_CUR_ID);

        selectChannel(currentList.getNextSelectedItem(id));
    }

    public void playNextChannelOrBack() {
        GridData selGd = null;
        if (isModeFile()) {
            playNextFileOrBack();
            return;
        }
        if (screenMode != ScreenMode.MultiView) {
            SharedSettings _set = SharedSettings.getInstance();
            long id = _set.getLongValueForKey(S_CUR_ID);

            selGd = currentList.getNextSelectedItem(id);
        } else {
            if (currentCameraIn2x2Mode + 1 < _2x2camerasData.size()) {
                currentCameraIn2x2Mode++;
                selGd = _2x2camerasData.get(currentCameraIn2x2Mode);
                if (selGd == null) {
                    currentCameraIn2x2Mode--;
                }
            }
            switch (playerFullScreen) {
                case player1:
                    player = player1;
                    break;
                case player2:
                    player = player2;
                    break;
                case player3:
                    player = player3;
                    break;
                case player4:
                    player = player4;
                    break;
            }
        }
        if (selGd == null) {
            if (screenMode != ScreenMode.MultiView) {

                player.Close();
                onBackPressed();
            }
            return;

        }


        selectChannel(selGd);
    }

    private void playNextChannelOrAgain() {
        if (isModeFile()) {
            playNextFileOrAgain();
            return;
        }
    }

    private void playNextFileOrAgain() {
        Log.i(TAG, "=>playNextFileOrAgain=");

        player.Close();
        GridData selGd = currentList.getNextSelectedItem(-1);

        if (selGd == null) {
            selGd = currentList.getFirstItem();
        }

        selectChannel(selGd);
    }

    private void selectPreviousChannel() {
        if (isModeFile()) {
            selectPreviousFile();
            return;
        }

        SharedSettings _set = SharedSettings.getInstance();
        long id = _set.getLongValueForKey(S_CUR_ID);

        selectChannel(currentList.getPreviousSelectedItem(id));
    }

    private void playPreviousChannelOrBack() {
        GridData selGd = null;
        if (isModeFile()) {
            playPreviousFileOrBack();
            return;
        }
        if (screenMode != ScreenMode.MultiView) {
            SharedSettings _set = SharedSettings.getInstance();
            long id = _set.getLongValueForKey(S_CUR_ID);

            selGd = currentList.getPreviousSelectedItem(id);
        } else {
            if (currentCameraIn2x2Mode - 1 >= 0 && currentCameraIn2x2Mode - 1 < _2x2camerasData.size()) {
                currentCameraIn2x2Mode--;
                selGd = _2x2camerasData.get(currentCameraIn2x2Mode);
                if (selGd == null) {
                    currentCameraIn2x2Mode++;
                }
            }
        }
        if (selGd == null) {
            if (screenMode != ScreenMode.MultiView) {

                player.Close();
                onBackPressed();
            }
            return;

        }

        selectChannel(selGd);
    }

    private void selectNextFile() {
        player.Close();
        selectChannel(currentList.getNextSelectedItem(-1));
    }

    private void playNextFileOrBack() {
        Log.i(TAG, "=>playNextFileOrBack=");

        player.Close();
        GridData selGd = currentList.getNextSelectedItem(-1);

        if (selGd == null) {
            onBackPressed();
            return;
        }
        selectChannel(selGd);
    }

    private void playPreviousFileOrBack() {
        Log.i(TAG, "=>playPreviousFileOrBack=");

        player.Close();
        GridData selGd = currentList.getPreviousSelectedItem(-1);

        if (selGd == null) {
            onBackPressed();
            return;
        }
        selectChannel(selGd);
    }

    private void selectPreviousFile() {
        player.Close();
        selectChannel(currentList.getPreviousSelectedItem(-1));
    }

    public void playerConnect(GridData gd) {
        if (player == null || gd == null)
            return;

        Log.i(TAG, "=>playerConnect " + gd.url + "," + gd.id + "," + player.getState());
        if ((screenMode != ScreenMode.MultiView) && !isModeFile() && m_cur_item != null && gd.id == m_cur_item.id &&
                player.getState() != PlayerState.Closed) {
            return;
        }

        m_cur_item = gd;

        if (!showPreview)
            hideControlPanelAndGrid();

        player.Close();

        String url = gd.url;
        if ((gd.user != null && gd.user.length() > 0) ||
                (gd.password != null && gd.password.length() > 0)) {
            url = "";
            String[] su = gd.url.split("://");
            if (su.length > 1) {
                url = su[0] + "://" + gd.user + ":" + gd.password + "@" + su[1];
            } else {
                url = "rtsp://" + gd.user + ":" + gd.password + "@" + su[0];
            }
        }

        Log.i(TAG, "=playerConnect url=" + url);

        String strText = gd.name;
        //Toast.makeText(MainActivity.this, strText, Toast.LENGTH_SHORT).show();

        textPanelPlayerCaptionText.setText(strText);

        SharedSettings.getInstance().loadPrefSettings();
        SharedSettings sett = SharedSettings.getInstance();

        isFileUrl = isUrlFile(gd.url);

        int connectionProtocol = sett.connectionProtocol;
        int connectionDetectionTime = sett.connectionDetectionTime;
        int connectionBufferingTime = sett.connectionBufferingTime;

        int decoderType = sett.decoderType;
        int rendererType = sett.rendererType;
        int rendererEnableColorVideo = sett.rendererEnableColorVideo;
        int rendererAspectRatioMode = mPanelIsVisible ? 0 : sett.rendererAspectRatioMode;
        int synchroEnable = sett.synchroEnable;
        int synchroNeedDropVideoFrames = sett.synchroNeedDropVideoFrames;

        if (mPanelIsVisible)
            player.backgroundColor(Color.parseColor("#DCE1E2"));
        else
            player.backgroundColor(Color.BLACK);

        // Connect and start playback
        Log.i(TAG, "=>playerConnect isStartedByIntent=" + (isFileUrl ? 1 : player.getConfig().getDataReceiveTimeout()));

        //player.setKey("27f6bee98ba8962f7a65e9b32542869e");
        //player.setStartLiveStreamPosition(30000);
        //player.getConfig().setStartPreroll(1);
        //player.setLiveStreamPath("\\storage\\path\\sizemaxvalue4096\\here");

        //SharedSettings.getInstance().rendererAspectRatioZoomModePercent = 100;
        player.Open(gd.url, connectionProtocol, connectionDetectionTime, connectionBufferingTime,
                decoderType,
                rendererType,
                synchroEnable,
                synchroNeedDropVideoFrames,
                rendererEnableColorVideo,
                rendererAspectRatioMode,
                isFileUrl ? 1 : player.getConfig().getDataReceiveTimeout(),
                sett.decoderNumberOfCpuCores,
                currentPlayerCallBacks);

    }

    public void playerConnect(final String url) {
        if (player == null || url.isEmpty())
            return;

        hideControlPanelAndGrid();
        //updatePlayerPanelControl(false);

        mCloseIconsIsVisible = false;
        //adapter_doc.notifyDataSetChanged();

        //player.setVisibility(View.VISIBLE);
        //progress_bar.setVisibility(View.VISIBLE);

        player.Close();
        //player_state = PlayerStates.ReadyForUse;

        SharedSettings.getInstance().loadPrefSettings();
        SharedSettings sett = SharedSettings.getInstance();

        isFileUrl = isUrlFile(url);

        int connectionProtocol = sett.connectionProtocol;
        int connectionDetectionTime = sett.connectionDetectionTime;
        int connectionBufferingTime = sett.connectionBufferingTime;

        int decoderType = sett.decoderType;
        int rendererType = sett.rendererType;
        int rendererEnableColorVideo = sett.rendererEnableColorVideo;
        int rendererEnableAspectRatio = guardedByOrientationIntValue(sett.rendererEnableAspectRatio);
        int synchroEnable = sett.synchroEnable;
        int synchroNeedDropVideoFrames = sett.synchroNeedDropVideoFrames;

        // Connect and start playback
        if (mPanelIsVisible)
            player.backgroundColor(Color.parseColor("#DCE1E2"));
        else
            player.backgroundColor(Color.BLACK);

        //player.setKey("27f6bee98ba8962f7a65e9b32542869e");
        //player.setStartLiveStreamPosition(30000);
        //player.getConfig().setStartPreroll(1);

        //SharedSettings.getInstance().rendererAspectRatioZoomModePercent = 100;
        player.Open(url, connectionProtocol, connectionDetectionTime, connectionBufferingTime,
                decoderType,
                rendererType,
                synchroEnable,
                synchroNeedDropVideoFrames,
                rendererEnableColorVideo,
                sett.rendererAspectRatioMode,
                isFileUrl ? 1 : player.getConfig().getDataReceiveTimeout(),
                sett.decoderNumberOfCpuCores,
                currentPlayerCallBacks);
    }

    public void playerConnectingFullScreen() {
        if (player == null)
            return;

        hideControlPanelAndGrid();
        //updatePlayerPanelControl(false);

        mCloseIconsIsVisible = false;
        //adapter_doc.notifyDataSetChanged();

        //player.setVisibility(View.VISIBLE);
        //progress_bar.setVisibility(View.VISIBLE);

        playNextFileOrAgain();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isLocked) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    ;

    @Override
    public void onBackPressed() {
        Log.i(TAG, "=>onBackPressed isStartedByIntent=" + isStartedByIntent);
        if (screenMode != ScreenMode.MultiView) {
            if (isLocked)
                return;

            if (!isStartedByIntent) {
                if (!mPanelIsVisible) {
                    {
                        if (screenMode == ScreenMode.OneView) {
                            player.Close();
                            playerContainer.setVisibility(View.INVISIBLE);
                            invalidateOptionsMenu();
                        }

                        showControlPanelAndGrid();
                    }
                } else {

                    if (currentList.GoBackFromSelectedItem())
                        return;

                    showExitDialog();
                }
                Log.i(TAG, "<=onBackPressed isStartedByIntent=" + isStartedByIntent);
                return;
            }

            if (!currentList.isItemExistByUrl(urlFromIntent)) {
                String sName = "External";
                sName += " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

                addChannel(streamsList, sName, urlFromIntent, "", "", 1, 1, "");
            }

            finish();
        } else {
            if (playersCon.getVisibility() == View.VISIBLE) {
                if (!isFullScreen) {
                    playersnames.setVisibility(View.INVISIBLE);
                    showControlPanelAndGrid();
                    playersCon.setVisibility(View.INVISIBLE);
                    fourCamerasGridVisible = false;
                    ArrayList<MediaPlayer> pl = new ArrayList<MediaPlayer>();
                    pl.add(player1);
                    pl.add(player2);
                    pl.add(player3);
                    pl.add(player4);
                    for (MediaPlayer item : pl) {
                        item.Close();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {

                        }
                    }


                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    getWindow().setAttributes(attrs);
                    hider.setup();


                } else
                    setTo2x2CamerasLayout();

                //player.setVisibility(View.GONE);

            } else {
                if (currentList.GoBackFromSelectedItem())
                    return;
                showExitDialog();
            }
        }
    }

    private void addM3UToList(String url) {
        try {
            M3U m3u = new M3U();
            m3u.getDataAndParse(url/*"http://watchmobiletv.net/tv_list.m3u"*/);
            List<M3U.M3UChannel> list = m3u.getChannelList();

            Log.i(TAG, "M3U size:" + list.size());
            for (int i = 0; i < list.size(); i++) {
                String ext;
                if (list.get(i).url.length() > 5)
                    ext = list.get(i).url.substring(list.get(i).url.length() - 5);
                else
                    ext = list.get(i).url;

                ext = ext.toLowerCase();
                Log.i(TAG, "M3U " + ext + " " + i + "url:" + list.get(i).url);

                // Change on  regular Expression
                if (!((ext.indexOf(".3ga") != -1) ||
                        (ext.indexOf(".a52") != -1) ||
                        (ext.indexOf(".aac") != -1) ||
                        (ext.indexOf(".ac3") != -1) ||
                        (ext.indexOf(".adt") != -1) ||
                        (ext.indexOf(".adts") != -1) ||
                        (ext.indexOf(".aif") != -1) ||
                        (ext.indexOf(".aifc") != -1) ||
                        (ext.indexOf(".aifc") != -1) ||
                        (ext.indexOf(".aiff") != -1) ||
                        (ext.indexOf(".amr") != -1) ||
                        (ext.indexOf(".aob") != -1) ||
                        (ext.indexOf(".ape") != -1) ||
                        (ext.indexOf(".awb") != -1) ||
                        (ext.indexOf(".caf") != -1) ||
                        (ext.indexOf(".dts") != -1) ||
                        (ext.indexOf(".flac") != -1) ||
                        (ext.indexOf(".it") != -1) ||
                        (ext.indexOf(".m4a") != -1) ||
                        (ext.indexOf(".m4p") != -1) ||
                        (ext.indexOf(".mid") != -1) ||
                        (ext.indexOf(".mka") != -1) ||
                        (ext.indexOf(".mlp") != -1) ||
                        (ext.indexOf(".mod") != -1) ||
                        (ext.indexOf(".mpa") != -1) ||
                        (ext.indexOf(".mp1") != -1) ||
                        (ext.indexOf(".mp2") != -1) ||
                        (ext.indexOf(".mp3") != -1) ||
                        (ext.indexOf(".mpc") != -1) ||
                        (ext.indexOf(".mpga") != -1) ||
                        (ext.indexOf(".oga") != -1) ||
                        (ext.indexOf(".ogg") != -1) ||
                        (ext.indexOf(".oma") != -1) ||
                        (ext.indexOf(".opus") != -1) ||
                        (ext.indexOf(".ra") != -1) ||
                        (ext.indexOf(".ram") != -1) ||
                        (ext.indexOf(".rmi") != -1) ||
                        (ext.indexOf(".s3m") != -1) ||
                        (ext.indexOf(".spx") != -1) ||
                        (ext.indexOf(".tta") != -1) ||
                        (ext.indexOf(".voc") != -1) ||
                        (ext.indexOf(".vqf") != -1) ||
                        (ext.indexOf(".w64") != -1) ||
                        (ext.indexOf(".wav") != -1) ||
                        (ext.indexOf(".wma") != -1) ||
                        (ext.indexOf(".wv") != -1) ||
                        (ext.indexOf(".xa") != -1) ||
                        (ext.indexOf(".xm") != -1))
                        ) {
                    Log.i(TAG, "M3U Add channel:" + ext + " " + i + " title:" + list.get(i).title + "url:" + list.get(i).url);
                    addChannel(streamsList, list.get(i).title, list.get(i).url, "", "", 1, 1, "");
                }
            }
        } catch (Exception e) {
        }
    }

    // calback addchannel dialog here
    @Override
    public void onSaveAddChannelDialog(String name, String url, String user, String password, int channel_id, int preview, boolean is_shot_taken, String image_file) {
        if (mAddChannelDialog.IS_EDIT_ID == (-1)) {
            if (name.toUpperCase().startsWith("M3U") == true ||
                    (url.toUpperCase().indexOf(".M3U") != -1 && url.toUpperCase().indexOf(".M3U8") == -1)) {
                addM3UToList(url);
            } else {
                addChannel(name, url, user, password, channel_id, preview, image_file);
            }
        } else {
            updateChannel(mAddChannelDialog.IS_EDIT_ID, name, url, user, password, channel_id, preview, (is_shot_taken ? image_file : ""));
        }

        if (isStartedByIntent) {
            showControlPanelAndGrid();
            isStartedByIntent = false;
        }

        selectCurrent();
    }

    @Override
    public void onCancelAddChannelDialog() {
        if (isStartedByIntent) {
            isStartedByIntent = false;
            finish();
            return;
        }
        selectCurrent();
    }

    @Override
    public void onLoadComplete(boolean ret) {
        refreshListEmptyState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isLocked)
            return false;

        if (!mPanelIsVisible)
            return false;

        getMenuInflater().inflate(R.menu.actions, menu);
        Log.i(TAG, "=onCreateOptionsMenu=");
        MenuItem item = menu.findItem(R.id.action_add);
        if (isModeFile()) {
            item.setVisible(false);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else {
            item.setVisible(true);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        MenuItem item_play = menu.findItem(R.id.action_play);
        MenuItem item_add = menu.findItem(R.id.action_add);
        if (isPlayerStarted()) {
            //playing
            item_play.setIcon(R.drawable.pl_3);
        } else {
            //stopped
            item_play.setIcon(R.drawable.pl_2);
        }

        if (isModeCameras()) {
            item_add.setIcon(R.drawable.action_cameras_refresh);
        } else {
            item_add.setIcon(R.drawable.pl);
        }

        item_play.setVisible(m_cur_item != null);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isLocked)
            return false;

        if (!mPanelIsVisible)
            return false;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isLocked)
            return false;

        if (!mPanelIsVisible)
            return false;

        int id = item.getItemId();
        switch (id) {
            case R.id.action_play:
                if (isPlayerStarted()) {
                    //stop
                    if (player != null) {
                        player.Close();
                        invalidateOptionsMenu();
                    }
                } else {
                    //run
                    selectChannel(m_cur_item);
                    if (player != null) {
                        player.Play();
                        invalidateOptionsMenu();
                    }
                }
                return true;
            case R.id.action_settings:
                isReturnedFromPreference = true;
                SharedSettings.getInstance().loadPrefSettings();

                Intent intentSettings = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivityForResult(intentSettings, 1);
                return true;
            case R.id.action_add:
                onChannelAdd();
                return true;
            case R.id.action_help:
                onHelp();
                return true;

            case R.id.action_send_log:
                Logger log = new Logger(this);
                log.sendLogByEmail("maxim@videoexpertsgroup.com", "", ""); // "maxim@videoexpertsgroup.com"
                return true;

            case R.id.action_exit:
                showExitDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int currentMode = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("view", -1);

        switch (currentMode) {
            case (0):
                screenMode = ScreenMode.Preview;
                break;
            case (1):
                screenMode = ScreenMode.OneView;
                break;
            case (2):
                screenMode = ScreenMode.MultiView;
                break;
            default:
                screenMode = ScreenMode.MultiView;
        }

        playerContainer.setVisibility(screenMode == ScreenMode.Preview ? View.VISIBLE : View.INVISIBLE);

        if (requestCode == 1) {

            SharedSettings.getInstance().loadPrefSettings();
            if (SharedSettings.getInstance().LockPlayerViewOrientation == 0)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            showPreview = guardedByBuildversionBooleanValue(SharedSettings.getInstance().showPreview);
            if (showPreview) {
                if (screenMode == ScreenMode.Preview)
                    showPlayerView();
            } else {
                hidePlayerView();
            }
        }
        currentList.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean bPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        Log.v(TAG, "onConfigurationChanged() bPortrait=" + bPortrait);

        if (player == null)
            return;

        if (mPanelIsVisible) {
            //player.UpdateView(false);
            player.getConfig().setAspectRatioMode(0);
            player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
            //player.UpdateView();
        } else {
//			player.UpdateView(guardedByOrientationIntValue(SharedSettings.getInstance().rendererEnableAspectRatio) == 1);
            Log.v(TAG, "onConfigurationChanged() rendererAspectRatioMode=" + SharedSettings.getInstance().rendererAspectRatioMode);
            player.getConfig().setAspectRatioMode(SharedSettings.getInstance().rendererAspectRatioMode);
            player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
            //player.UpdateView();
        }

    }

//    public void onInit()
//    {
//		Log.v(TAG, "onInit() ="+adapter_doc);
//    	if(adapter_doc != null){
//    		refreshListEmptyState();
//
//    		if (adapter_doc.getState() == TabState.State_Streams)
//    		{
//    			// try refresh list
//	        	mGridList.clear();
//	            Cursor c = mDbHelper.readChannelForPresentation();
//	            while (c.moveToNext()) {
//	                mGridList.add(new GridData(c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_NAME)),
//	                        c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_URL)),
//	                        c.getString(c.getColumnIndexOrThrow(ChannelListTable.USER)),
//	                        c.getString(c.getColumnIndexOrThrow(ChannelListTable.PASSWORD)),
//	                        c.getInt(c.getColumnIndexOrThrow(ChannelListTable._ID /*CHANNEL_ID*/)),
//	                        c.getInt(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_IMAGE_URL)),
//	                        c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_IMAGE_URL_STR))));
//	            }
//
//	            adapter_doc.update_list(mGridList);
//	            adapter_doc.notifyDataSetChanged();
//    		}
////    		if (adapter_doc.getState() == TabState.State_Files)
////    		{
////	            adapter_doc.update_list(mGridList);
////	            adapter_doc.notifyDataSetChanged();
////    		}
//    		return;
//    	}
//
//    	mGridList.clear();
//        Cursor c = mDbHelper.readChannelForPresentation();
//        while (c.moveToNext()) {
//            mGridList.add(new GridData(c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_NAME)),
//                    c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_URL)),
//                    c.getString(c.getColumnIndexOrThrow(ChannelListTable.USER)),
//                    c.getString(c.getColumnIndexOrThrow(ChannelListTable.PASSWORD)),
//                    c.getInt(c.getColumnIndexOrThrow(ChannelListTable._ID /*CHANNEL_ID*/)),
//                    c.getInt(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_IMAGE_URL)),
//                    c.getString(c.getColumnIndexOrThrow(ChannelListTable.CHANNEL_IMAGE_URL_STR))));
//        }
//
//        Log.v(TAG, "=mGridList size="+mGridList.size());
//
//		adapter_doc = new GridAdapter(MainActivity.this, mGridList);
//		list_docview.setAdapter(adapter_doc);
//		adapter_doc.set_sel(null);
//		adapter_doc.update_dir(null);
//		adapter_doc.setState(TabState.State_Files);
//        adapter_doc.notifyDataSetChanged();
//
//		refreshListEmptyState();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isReturnedFromPreference) {
            isReturnedFromPreference = false;
            return;
        }

        if (hider != null)
            hider.showWithResize();

        Log.i(TAG, "=>onStart " + isStartedByIntent);

        currentList.Refresh();
        refreshListEmptyState();

        if (player != null)
            player.onStart();

        if (mAddChannelDialog.isShowing() && !isStartedByIntent) {
            mAddChannelDialog.refreshPlayer();
            return;
        }
        singleModePlayerCallBacks.player_state_error = PlayerCallBacks.PlayerStatesError.None;
        Player1CallBacks.player_state_error = PlayerCallBacks.PlayerStatesError.None;
        Player2CallBacks.player_state_error = PlayerCallBacks.PlayerStatesError.None;
        Player3CallBacks.player_state_error = PlayerCallBacks.PlayerStatesError.None;
        Player4CallBacks.player_state_error = PlayerCallBacks.PlayerStatesError.None;

        if (!isStartedByIntent) {
            isLocked = false;
            updatePlayerPanelControl(!isLocked, isLocked);

            player.Close();
            showControlPanelAndGrid();


            if (!showPreview) {
                hidePlayerView();
            }

            if (currentList.getList().size() > 0) {
                SharedSettings _set = SharedSettings.getInstance();
                long id = _set.getLongValueForKey(S_CUR_ID);

                if (!firstRunned || id <= 0 || !isSameListAsTabId()) {
                    //selectCurrent();
                    return;
                }

//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//                alertDialog.setMessage(getResources().getString(R.string.dialog_autostart_message));
//
//                alertDialog.setPositiveButton(getResources().getString(R.string.dialog_autostart_yes), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectCurrent();
//                        firstRunned = false;
//                    }
//                });
//
//                alertDialog.setNegativeButton(getResources().getString(R.string.dialog_autostart_no), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                        firstRunned = false;
//
//                        SharedSettings _set = SharedSettings.getInstance();
//                        _set.setLongValueForKey(S_CUR_ID, -1); // clear selection
//                        _set.savedTabNumForSavedId = -1;
//                        _set.savePrefSettings();
//                    }
//                });
//
//                alertDialog.show();
            }
        }
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "<=onStop");
        if (player != null)
            player.onStop();

        super.onStop();

        if (isReturnedFromPreference)
            return;
    }

    @Override
    protected void onPause() {
        //unregister FileObserver
        /*
		Log.i(TAG, "=onPause unregister ContentObserver, file_observer="+file_observer);
		if(file_observer != null){
	    	getContentResolver().unregisterContentObserver(file_observer);
		}
		file_observer = null;
		*/

        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playersCon.getVisibility() == View.VISIBLE && (screenMode == ScreenMode.MultiView)) {
            if (!isFullScreen) {
                playersnames.setVisibility(View.INVISIBLE);
                showControlPanelAndGrid();
                player1.Close();
                player2.Close();
                player3.Close();
                player4.Close();
                playersCon.setVisibility(View.INVISIBLE);
                fourCamerasGridVisible = false;
            } else {
                setTo2x2CamerasLayout();
                playersnames.setVisibility(View.INVISIBLE);
                showControlPanelAndGrid();
                player1.Close();
                player2.Close();
                player3.Close();
                player4.Close();
                playersCon.setVisibility(View.INVISIBLE);
                fourCamerasGridVisible = false;
            }
        }
        //register FileObserver
        if (file_observer_v == null) {
            file_observer_v = new MyContentObserver(singleModePlayerCallBacks.handler);
            file_observer_a = new MyContentObserver(singleModePlayerCallBacks.handler);

            //File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            //File fp = f.getParentFile();

            //Uri uri = Uri.fromParts("content", "file", fp.getAbsolutePath());
            //Uri uri = Uri.fromFile(fp);
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Log.i(TAG, "=onResume register registerContentObserver video, uri=" + uri);
            getContentResolver().
                    registerContentObserver(
                            uri,
                            false,
                            file_observer_v);

            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Log.i(TAG, "=onResume register registerContentObserver audio, uri=" + uri);
            getContentResolver().
                    registerContentObserver(
                            uri,
                            false,
                            file_observer_a);

        }

        if (is_file_content_dirty) {
            is_file_content_dirty = false;

            if (isCurrentFilesList()) {
                currentList.Refresh();
            }
        }

    }


    @Override
    protected void onDestroy() {

        //unregister FileObserver
        Log.i(TAG, "=onDestroy unregister ContentObserver, file_observer=" + file_observer_v);
        if (file_observer_v != null) {
            getContentResolver().unregisterContentObserver(file_observer_v);
        }
        if (file_observer_a != null) {
            getContentResolver().unregisterContentObserver(file_observer_a);
        }
        file_observer_v = null;
        file_observer_a = null;

        if (player != null) {
            player.Close();
            player.onDestroy();
        }
        player = null;

        if (currentList != null)
            currentList.Close();

//		if (playerPanelControlTimer != null)
//		{
//			playerPanelControlTimer.cancel();
//			playerPanelControlTimer.purge();
//			playerPanelControlTimer = null;
//	        Log.i(TAG, "Stop timer");
//		}

        if (playerPanelControlTask != null) {
            playerPanelControlTask.cancel(true);
            playerPanelControlTask = null;
            Log.i(TAG, "Stop Task");
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            list_docview.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListListener);
            list_docview.getViewTreeObserver().removeGlobalOnLayoutListener(layoutEmptyListener);
        } else {
            list_docview.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListListener);
            list_docview.getViewTreeObserver().removeOnGlobalLayoutListener(layoutEmptyListener);
        }

        Log.i(TAG, "<=onDestroy");
        super.onDestroy();
    }


    void onChannelAdd() {
        if (isModeFile())
            return;

        Log.v(TAG, "=>onChannelAdd");
        if (isModeCameras()) {
            currentList.Load();
            return;
        }

        mAddChannelDialog.IS_EDIT_ID = -1;
        if (mAddChannelDialog.isAdded())
            return;
        else {
            //player.Close();

            mAddChannelDialog.show(getFragmentManager(), "TAG");
            mCloseIconsIsVisible = false;
//            adapter_doc.notifyDataSetChanged();
        }
        Log.v(TAG, "<=onChannelAdd");
    }

    void onChannelAdd(final String name, final String url) {
        if (isModeFile())
            return;

        Log.v(TAG, "=>onChannelAdd");

        mAddChannelDialog.IS_EDIT_ID = -1;
        if (mAddChannelDialog.isAdded())
            return;
        else {
            //player.Close();

            mAddChannelDialog.setContentValues(name, url);
            mAddChannelDialog.show(getFragmentManager(), "TAG");
            mCloseIconsIsVisible = false;
//            adapter_doc.notifyDataSetChanged();
        }
        Log.v(TAG, "<=onChannelAdd");
    }


    void onChannelEdit(int x, int y) {
        int sel_channel = list_docview.pointToPosition(x, y);//chs_list.GetSelectedChannel();
        if (sel_channel == AdapterView.INVALID_POSITION)
            return;

        try {
            GridData gd = (GridData) list_docview.getItemAtPosition(sel_channel);
            onChannelEdit(gd);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    void onChannelEdit(GridData gd) {
        if (isModeFile())
            return;

        if (gd == null)
            return;

        mAddChannelDialog.IS_EDIT_ID = gd.id;
        if (mAddChannelDialog.isAdded())
            return;
        else {
            //player.Close();

            mAddChannelDialog.setContentValues(gd.name, gd.url);
            mAddChannelDialog.show(getFragmentManager(), "TAG");
            mCloseIconsIsVisible = false;
//            adapter_doc.notifyDataSetChanged();
        }
    }

//	void onChannelDelete()
//	{
//		if (mGridList == null || mGridList.size() <= 0)
//			return;
//
//		Log.v(TAG, "=>onChannelDelete");
//        mCloseIconsIsVisible = !mCloseIconsIsVisible;
//        adapter_doc.notifyDataSetChanged();
//	}

    void onHelp() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);
    }

    //  @SuppressLint("NewApi")
    private void showControlPanelAndGrid() {
        mPanelIsVisible = true;

        if (currentList != null)
            currentList.StartThumbnailUpdate();

        updatePlayerPanelControl(false, isLocked);

        FrameLayout.LayoutParams lpc = (FrameLayout.LayoutParams) playerContainer.getLayoutParams();
        final float scale = getResources().getDisplayMetrics().density;
        int pixelsW = (int) (previewWidth * scale + 0.5f);
        int pixelsH = (int) (previewHeight * scale + 0.5f);

        Log.v(TAG, "=>showControlPanelAndGrid " + pixelsW + "x" + pixelsH);

        lpc.width = pixelsW;
        lpc.height = pixelsH;
        lpc.rightMargin = (int) (previewRightMargin * scale + 0.5f);
        lpc.bottomMargin = (int) (previewBottomMargin * scale + 0.5f);

        playerContainer.setLayoutParams(lpc);

        // add border
        playerContainer.setPadding(0, 0, 3, 3);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            playerContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.layout_border));
        } else {
            playerContainer.setBackground(getResources().getDrawable(R.drawable.layout_border));
        }

        if (player != null) {
            player.backgroundColor(Color.parseColor("#DCE1E2"));
            //player.UpdateView(false);
            player.getConfig().setAspectRatioMode(0);
            player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
            player.UpdateView();
        }

        player.setKeepScreenOn(false);

        if (getActionBar() != null)
            getActionBar().show();

        if (SharedSettings.getInstance().AllowFullscreenMode) {
            hider.showWithResize();
        }

        picPreviewLeftTop.setVisibility(View.VISIBLE);
        picPreviewRightBottom.setVisibility(View.GONE);

        list_docview.setVisibility(View.VISIBLE);

        invalidateOptionsMenu();

        if (player != null && !showPreview) {
            player.Close();
            hidePlayerView();
        }
    }

    private void hideControlPanelAndGrid() {
        Log.v(TAG, "=>hideControlPanelAndGrid");
        mPanelIsVisible = false;

        if (currentList != null)
            currentList.StopThumbnailUpdate();

        if (player != null) {
            player.backgroundColor(Color.BLACK);
//    		player.UpdateView(guardedByOrientationIntValue(SharedSettings.getInstance().rendererEnableAspectRatio) == 1);
            player.getConfig().setAspectRatioMode(SharedSettings.getInstance().rendererAspectRatioMode);
            player.getConfig().setAspectRatioZoomModePercent(SharedSettings.getInstance().rendererAspectRatioZoomModePercent);
            player.UpdateView();
        }

        picPreviewLeftTop.setVisibility(View.GONE);
        picPreviewRightBottom.setVisibility(View.GONE);

        list_docview.setVisibility(View.GONE);
        if (getActionBar() != null)
            getActionBar().hide();


        if (SharedSettings.getInstance().AllowFullscreenMode && !isFullScreen) {
            hider.hide();
        }

        FrameLayout.LayoutParams lpc = (FrameLayout.LayoutParams) playerContainer.getLayoutParams();
        lpc.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lpc.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lpc.rightMargin = 0;
        lpc.bottomMargin = 0;
        playerContainer.setLayoutParams(lpc);

        // remove border
        playerContainer.setPadding(0, 0, 0, 0);
        playerContainer.setBackgroundResource(android.R.color.transparent);
        ;

        player.setKeepScreenOn(true);
        invalidateOptionsMenu();

        if (!showPreview)
            showPlayerView();
    }

    public void addChannel(String name, String url, String user, String password, int channel_id, int preview, String image_file) {
        currentList.AddItem(name, url, user, password, channel_id, preview, image_file);
        refreshListEmptyState();
    }

    public void addChannel(final GridAdapter list, String name, String url, String user, String password, int channel_id, int preview, String image_file) {
        if (list == null)
            return;

        list.AddItem(name, url, user, password, channel_id, preview, image_file);
        refreshListEmptyState();
    }

    public void updateChannel(long id, String name, String url, String user, String password, int channel_id, int preview, String image_file) {
        //find
        currentList.UpdateItem(id, name, url, user, password, channel_id, preview, image_file);
    }


    public void onDeleteChannel(final GridData gd) {
        if (isModeFile())
            return;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        String sMessage = getResources().getString(R.string.dialog_delete_channel) + "'" + gd.name + "'?";
        alertDialog.setMessage(sMessage);

        alertDialog.setPositiveButton(getResources().getString(R.string.dialog_exit_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteChannel(gd.id);
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.dialog_exit_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
        return;
    }

    public void deleteChannel(long id) {
        SharedSettings _set = SharedSettings.getInstance();
        long saved_id = _set.getLongValueForKey(S_CUR_ID);
        if (saved_id == id) {
            _set.setLongValueForKey(S_CUR_ID, -1);
            _set.savedTabNumForSavedId = -1;
            _set.savePrefSettings();
        }

        currentList.DeleteItem(id);
        refreshListEmptyState();
        if (m_cur_item != null && m_cur_item.id == id) {
            player.Close();
        }
    }

    public boolean isUrlExist(final String url) {
        return currentList.isItemExistByUrl(url);
    }

    public int mOldMsg = 0;


    private void updatePlayerPanelControl(final boolean visible, final boolean locked) {
        if (!visible) {
            isPlayerPanelVisible = false;
            if (layoutPlayerPanel.getVisibility() == View.VISIBLE)
                layoutPlayerPanel.setVisibility(View.GONE);

//			if (playerPanelControlTimer != null)
//			{
//				playerPanelControlTimer.cancel();
//				playerPanelControlTimer.purge();
//				playerPanelControlTimer = null;
//		        Log.i(TAG, "Stop timer");
//			}
            if (playerPanelControlTask != null) {
                playerPanelControlTask.cancel(true);
                playerPanelControlTask = null;
                Log.i(TAG, "Stop Task");
            }

            lockPlayer(locked);

            return;
        }

        lockPlayer(false);

        isPlayerPanelVisible = true;
        if (layoutPlayerPanel.getVisibility() != View.VISIBLE)
            layoutPlayerPanel.setVisibility(View.VISIBLE);

        if (player == null)
            return;

        long saved_id = SharedSettings.getInstance().getLongValueForKey(S_CUR_ID);
        GridData gdf = currentList.getItem(saved_id);

        if (isModeFile())
            gdf = currentList.getSelectedItem();

        textPanelPlayerCaptionText.setText(gdf != null ? gdf.name : "");

        String decoderType = player.IsHardwareDecoding() ? getResources().getString(R.string.player_control_decoder_hw) :
                getResources().getString(R.string.player_control_decoder_sw);
        textPanelPlayerCaptionDecoderType.setText(decoderType);

        refreshPlayerPanelControlVisibleTimer();
        Log.i(TAG, "mediaLivePositionUpdate from updatePlayerPanelControl.");
        //mediaLivePositionUpdate();
        //mediaPositionTimerMethod();

//	    playerPanelControlTimer = new Timer();
//	    playerPanelControlTimer.schedule(new TimerTask()
//	    {
//	        @Override
//	        public void run()
//	        {
//	        	mediaLivePositionTimerMethod();
//	        	//mediaPositionTimerMethod();
//	        }
//
//	    }, 0, 1000);

        if (playerPanelControlTask == null) {
            //playerPanelControlTask.cancel(true);
            //playerPanelControlTask = null;
            playerPanelControlTask = new PlayerPanelControlVisibleTask();
            executeAsyncTask(playerPanelControlTask, "");
        }


        if (SharedSettings.getInstance().AllowFullscreenMode)
            hider.resize();
    }

    public void lockOrientation() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int tempOrientation = getResources().getConfiguration().orientation;
        int orientation = 0;
        switch (tempOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                else
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                else
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
        setRequestedOrientation(orientation);
    }

    private void lockPlayer(final boolean lock) {
        if (hider != null)
            hider.lock(lock);

        if (lock) {

            if (layoutPlayerLocked.getVisibility() != View.VISIBLE)
                layoutPlayerLocked.setVisibility(View.VISIBLE);

            lockOrientation();
        } else {
            if (layoutPlayerLocked.getVisibility() == View.VISIBLE)
                layoutPlayerLocked.setVisibility(View.GONE);

            if (SharedSettings.getInstance().LockPlayerViewOrientation == 0)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void refreshPlayerPanelControlVisibleTimer() {
        //currentPlayerPanelControlVisibleTime = waitForHidePlayerPanelControl;
        if (playerPanelControlTask != null)
            playerPanelControlTask.restartTimer();
    }

    public void updatePlayerPanelControlButtons(final boolean lock, final boolean pause, int aspect_ratio_mode) {
        if (player == null)
            return;

        String decoderType = player.IsHardwareDecoding() ? getResources().getString(R.string.player_control_decoder_hw) :
                getResources().getString(R.string.player_control_decoder_sw);
        textPanelPlayerCaptionDecoderType.setText(decoderType);

        if (pause)
            playerPanelControlPlay.setImageDrawable(imgPlayPause[1]);
        else
            playerPanelControlPlay.setImageDrawable(imgPlayPause[0]);
        playerPanelControlPlay.getDrawable().setLevel(5000);


        aspect_ratio_mode++;
        if (aspect_ratio_mode > 4)
            aspect_ratio_mode = 0;
        playerPanelControlScreen.setImageDrawable(imgAspects[aspect_ratio_mode]);

        playerPanelControlScreen.getDrawable().setLevel(5000);

        invalidateOptionsMenu();
    }

    synchronized private void mediaPositionUpdate() {
        Log.i(TAG, "=mediaPositionUpdate= ");
        if (player == null)
            return;

        //if (currentPlayerPanelControlVisibleTime == 0)
        if (playerPanelControlTask == null || playerPanelControlTask.getTimer() <= 0) {
            if (SharedSettings.getInstance().AllowFullscreenMode && !mPanelIsVisible)
                hider.hide();
            updatePlayerPanelControl(false, isLocked);
            return;
        }

        //currentPlayerPanelControlVisibleTime--;

        long duration = player.getStreamDuration();
        long position = player.getStreamPosition();

        Log.i(TAG, "=mediaPositionUpdate= " + playerPanelControlTask.getTimer() + ", " + position + ", " + duration);
        if (player != null && (player.getState() != PlayerState.Started &&
                player.getState() != PlayerState.Opened &&
                player.getState() != PlayerState.Stopped &&
                player.getState() != PlayerState.Paused))
            duration = 0;

        if (duration > 0) duration -= 1000;

        if (duration <= 0 || (duration > (60 * 60 * 12 * 1000))) // guard for 12 hours
        {
            position = 0;
            duration = (60 * 60 * 12 * 1000);
        }

        if (position > duration)
            position = duration;

        Log.i(TAG, "=mediaPositionUpdate= " + position + ", " + duration);

        if ((int) position != seekPanelPlayerControlSeekbar.getProgress()) {
            textPanelPlayerControlPosition.setText(convertStreamPositionToTime(position / 1000));
            seekPanelPlayerControlSeekbar.setProgress((int) position); // Set it to zero so it will start at the left-most edge
        }

        if ((int) duration != seekPanelPlayerControlSeekbar.getMax()) {
            seekPanelPlayerControlSeekbar.setMax((int) duration);
        }
        textPanelPlayerControlDuration.setText(convertStreamPositionToTime(duration / 1000));
    }

    synchronized private void mediaLivePositionUpdate() {
        Log.i(TAG, "=mediaLivePositionUpdate= ");
        if (player == null)
            return;

        //if (currentPlayerPanelControlVisibleTime == 0)
        if (playerPanelControlTask == null || playerPanelControlTask.getTimer() <= 0) {
            if (SharedSettings.getInstance().AllowFullscreenMode && !mPanelIsVisible)
                hider.hide();
            updatePlayerPanelControl(false, isLocked);
            return;
        }

        Position pos = player.getLiveStreamPosition();
        if (pos == null)
            return;

        if (pos.getStreamType() != 2) {
            mediaPositionUpdate();
            return;
        }

        //currentPlayerPanelControlVisibleTime--;

        updatePlayerPanelControlButtons(isLocked, (player.getState() == PlayerState.Started),
                SharedSettings.getInstance().rendererAspectRatioMode);

        long stream_position = player.getRenderPosition();
        if (lockChangePosition && lockedLatestStreamPosition == -1)
            lockedLatestStreamPosition = (int) stream_position;

        long duration = pos.getDuration();
        long first = pos.getFirst();
        long current = player.getRenderPosition();//pos.getCurrent();
        long last = pos.getLast();

        duration = last - first;
        current = current - first;

        Log.i(TAG, "mediaLivePositionTimerMethod: " + lockChangePosition + ", cur: " + current + ", touched: " + lockedChangePosition);
        if (lockChangePosition && lockedLatestStreamPosition != -1 &&
                lockedLatestStreamPosition != (int) stream_position) {
            lockChangePosition = false;
            lockedLatestStreamPosition = -1;
        }

        if (lockChangePosition)
            current = lockedChangePosition;

        if (player != null && (player.getState() != PlayerState.Started &&
                player.getState() != PlayerState.Opened &&
                player.getState() != PlayerState.Stopped &&
                player.getState() != PlayerState.Paused))
            duration = 0;

//        		textPanelPlayerControlPosition.setText(convertStreamPositionToTime2(first));
//    			textPanelPlayerControlDuration.setText(convertStreamPositionToTime2(last));

        //if (duration > 0) duration--;

        if (duration <= 0 || (duration > (60 * 60 * 12 * 1000))) // guard for 12 hours
        {
            current = 0;
            duration = (60 * 60 * 12 * 1000);
        }

        if (current > duration)
            current = duration;

        //Log.i(TAG, "=mediaPositionTimerMethod= " + position + ", " + duration);

        if ((int) current != seekPanelPlayerControlSeekbar.getProgress()) {
            textPanelPlayerControlPosition.setText("-" + convertStreamPositionToTime2(last / 1000 - first / 1000));
            seekPanelPlayerControlSeekbar.setProgress((int) current); // Set it to zero so it will start at the left-most edge
        }

        if ((int) duration != seekPanelPlayerControlSeekbar.getMax()) {
            seekPanelPlayerControlSeekbar.setMax((int) duration);
        }
        textPanelPlayerControlDuration.setText("Live"/*convertStreamPositionToTime2(last/1000)*/);
    }

    private void mediaPositionTimerMethod() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "=mediaPositionTimerMethod= ");
                mediaPositionUpdate();
            }
        });
    }

    private void mediaLivePositionTimerMethod() {
        Log.i(TAG, "=mediaLivePositionTimerMethod= ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mediaLivePositionUpdate();
            }
        });
    }


    private String convertStreamPositionToTime(long Time) {
        long tns, thh, tmm, tss;
        tns = Time;
        thh = tns / 3600;
        tmm = (tns % 3600) / 60;
        tss = (tns % 60);

        return String.format("%02d:%02d:%02d", thh, tmm, tss);
    }

    private String convertStreamPositionToTime2(long Time) {
        long tns, thh, tmm, tss;
        tns = Time;
        thh = tns / 3600;
        tmm = (tns % 3600) / 60;
        tss = (tns % 60);

        return String.format("%02d:%02d:%02d", thh, tmm, tss);
    }

//	private int convertStreamPositionToSeekbarValue(long Time)
//	{
//		if (Time < Integer.MAX_VALUE)
//			return (int)Time;
//
//
//	}


    public void hideProgressView(MediaPlayer currentPlayer) {
        if (screenMode != ScreenMode.MultiView)
            progress_bar.setVisibility(View.GONE);
        else if (fourCamerasGridVisible) {
            if (currentPlayer == player1)
                loaderIndicator1.setVisibility(View.GONE);
            else if (currentPlayer == player2)
                loaderIndicator2.setVisibility(View.GONE);
            else if (currentPlayer == player3)
                loaderIndicator3.setVisibility(View.GONE);
            else if (currentPlayer == player4)
                loaderIndicator4.setVisibility(View.GONE);

        }

        boolean isVisibleDisconnect = (!isModeFile() && (currentPlayerCallBacks.player_state_error == PlayerCallBacks.PlayerStatesError.Disconnected));
        picStatusDisconneted.setVisibility(isVisibleDisconnect ? View.VISIBLE : View.INVISIBLE);
        if (currentPlayerCallBacks != null)
            Log.i(TAG, "=hideProgressView= " + currentPlayerCallBacks.player_state_error);
    }

    public void showProgressView(MediaPlayer currentPlayer) {
        if (screenMode != ScreenMode.MultiView)
            progress_bar.setVisibility(View.VISIBLE);
        else {
            if (currentPlayer == player1)
                loaderIndicator1.setVisibility(View.VISIBLE);
            else if (currentPlayer == player2)
                loaderIndicator2.setVisibility(View.VISIBLE);
            else if (currentPlayer == player3)
                loaderIndicator3.setVisibility(View.VISIBLE);
            else if (currentPlayer == player4)
                loaderIndicator4.setVisibility(View.VISIBLE);

        }
        picStatusDisconneted.setVisibility(View.INVISIBLE);
        invalidateOptionsMenu();
        Log.i(TAG, "=showProgressView= " + currentPlayerCallBacks.player_state_error);
    }

    private void hidePlayerView() {
        if (player == null)
            return;

        //playerContainer.setAlpha(0.0f);
        playerContainer.setVisibility(View.GONE);
    }

    private void showPlayerView() {
        if (player == null)
            return;

        //playerContainer.setAlpha(1.0f);
        playerContainer.setVisibility(View.VISIBLE);
    }

    private void refreshListEmptyState() {
        if (currentList == null)
            return;

        Log.i(TAG, "=refreshListEmptyState= " + currentList.isEmpty());
        if (!currentList.isEmpty() || isModeFile()) {
            if (viewListEmpty.getVisibility() != View.INVISIBLE)
                viewListEmpty.setVisibility(View.INVISIBLE);
            if (list_docview.getVisibility() != View.VISIBLE)
                list_docview.setVisibility(View.VISIBLE);
        } else {
            if (isCurrentCamerasList())
                viewListEmpty.setText(getResources().getString(R.string.main_cameras_empty_list));
            else
                viewListEmpty.setText(getResources().getString(R.string.main_streams_empty_list));

            if (list_docview.getVisibility() != View.INVISIBLE)
                list_docview.setVisibility(View.INVISIBLE);
            if (viewListEmpty.getVisibility() != View.VISIBLE)
                viewListEmpty.setVisibility(View.VISIBLE);

            mCloseIconsIsVisible = false;
            invalidateOptionsMenu();
        }
    }

    private boolean printResolutionDevice() {
        boolean isTab = false;
        int screenSize = (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK);
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                Log.i(TAG, "Density: Large screen");
                isTab = true;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                Log.i(TAG, "Density: Normal screen");
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                Log.i(TAG, "Density: Small screen");
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                Log.i(TAG, "Density: XLarge screen");
                isTab = true;
                break;
            default:
                Log.i(TAG, "Density: Screen size is neither large, normal or small " + screenSize);
        }

        int density = getResources().getDisplayMetrics().densityDpi;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                Log.i(TAG, "Density: LDPI");
                densityDpiString = "_ldpi";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                Log.i(TAG, "Density: MDPI");
                densityDpiString = "_mdpi";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                Log.i(TAG, "Density: HDPI");
                densityDpiString = "_hdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                Log.i(TAG, "Density: XHDPI");
                densityDpiString = "_xhdpi";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                Log.i(TAG, "Density: XXHIGH");
                densityDpiString = "_xxhdpi";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                Log.i(TAG, "Density: XXXHIGH");
                densityDpiString = "_xxhdpi";
                break;
            case DisplayMetrics.DENSITY_TV:
                Log.i(TAG, "Density: TV");
                densityDpiString = "_hdpi";
                break;
            default:
                Log.i(TAG, "Density: " + density);
                break;

        }

        Display display = getWindowManager().getDefaultDisplay();
        if (display == null)
            return true;

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.i(TAG, "Size: width - " + width + ", height - " + height + ", is low resolution device - " + isLowResolutionDevice());

        if (!isTab) {
            if ((width > 480 && height > 800) || (height > 480 && width > 800)) {
                previewWidth = 180;
                previewHeight = 140;
            } else if ((width > 480 && height > 320) || (height > 480 && width > 320)) {
                previewWidth = 180;
                previewHeight = 120;
            } else {
                previewWidth = 120;
                previewHeight = 100;
            }
        }

        if (imgPlayPause == null)
            imgPlayPause = new Drawable[]{
                    getPictureById("player_play" + densityDpiString + "_scaled", R.drawable.class),
                    getPictureById("player_pause" + densityDpiString + "_scaled", R.drawable.class)
            };

        if (imgAspects == null)
            imgAspects = new Drawable[]{
                    getPictureById("player_screen_stretch" + densityDpiString + "_scaled", R.drawable.class),
                    getPictureById("player_screen_aspect" + densityDpiString + "_scaled", R.drawable.class),
                    getPictureById("player_screen_crop" + densityDpiString + "_scaled", R.drawable.class),
                    getPictureById("player_screen_realsize" + densityDpiString + "_scaled", R.drawable.class),
                    getPictureById("player_screen_zoom" + densityDpiString + "_scaled", R.drawable.class)
            };

        for (Drawable image : imgPlayPause)
            image.setLevel(5000);

        for (Drawable image : imgAspects)
            image.setLevel(5000);

        return true;
    }

    private boolean isLowResolutionDevice() {
        int screenSize = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL &&
                screenSize != Configuration.SCREENLAYOUT_SIZE_NORMAL)
            return false;

        int density = getResources().getDisplayMetrics().densityDpi;
        if (density != DisplayMetrics.DENSITY_LOW &&
                density != DisplayMetrics.DENSITY_MEDIUM &&
                density != DisplayMetrics.DENSITY_HIGH)
            return false;

        Display display = getWindowManager().getDefaultDisplay();
        if (display != null) {
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            if ((width > 480 && height > 800) || (height > 480 && width > 800))
                return false;

            Log.i(TAG, "Size: width - " + width + ", height - " + height);
            return true;
        }

        return true;
    }

    public Drawable getPictureById(String resourceName, Class<?> c) {
        try {
            Log.i(TAG, "getPictureById: " + resourceName);
            Field idField = c.getDeclaredField(resourceName);
            return getResources().getDrawable(idField.getInt(idField));
        } catch (Exception e) {
            throw new RuntimeException("No resource ID found for: " + resourceName + " / " + c, e);
        }
    }

    private int guardedByOrientationIntValue(int value) {
        boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        Log.i(TAG, "aspect ratio: " + (bPort ? 1 : value));
        return (bPort ? 1 : value);
    }

    private boolean guardedByBuildversionBooleanValue(boolean value) {
        boolean available = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
        return (available ? value : false);
    }

    private class PlayerPanelControlVisibleTask extends AsyncTask<String, Void, Boolean> {
        private long time;
        private int nAttempt = 10;

        public int getTimer() {
            return nAttempt;
        }

        ;

        public void restartTimer() {
            nAttempt = 10;
        }

        ;

        public void clearTimer() {
            nAttempt = 0;
        }

        ;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            time = System.currentTimeMillis();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Runnable uiRunnable = null;
            uiRunnable = new Runnable() {
                public void run() {
                    //Log.i(TAG, "mediaLivePositionUpdate from thread " + nAttempt);
                    mediaLivePositionUpdate();

                    synchronized (this) {
                        this.notify();
                    }
                }
            };

            boolean stop = false;
            try {
                do {
                    //Log.i(TAG, "doInBackground thread before " + nAttempt);
                    synchronized (uiRunnable) {
                        runOnUiThread(uiRunnable);
                        try {
                            uiRunnable.wait();
                        } catch (InterruptedException e) {
                            stop = true;
                        }
                    }

                    if (stop) break;

                    Thread.sleep(1000);
                    nAttempt--;
                    //Log.i(TAG, "doInBackground thread after " + nAttempt);
                }
                while (nAttempt > 0 && !isCancelled());
            } catch (Exception e) {
            }

            clearTimer();
            synchronized (uiRunnable) {
                runOnUiThread(uiRunnable);
                try {
                    uiRunnable.wait();
                } catch (InterruptedException e) {
                    stop = true;
                }
            }

            //mediaLivePositionTimerMethod();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            playerPanelControlTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            playerPanelControlTask = null;
        }
    }

    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }

    protected float dpFromPx(float px) {
        return (px / getResources().getDisplayMetrics().density);
    }

    protected float pxFromDp(float dp) {
        return (dp * getResources().getDisplayMetrics().density);
    }

    final public static String TAG = "MainActivity";

}
