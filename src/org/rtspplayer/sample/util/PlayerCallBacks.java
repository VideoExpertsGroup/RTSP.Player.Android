package org.rtspplayer.sample.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.rtspplayer.sample.activity.MainActivity;

import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.MediaPlayer;

/**
 * Created by alexey on 23.11.16.
 */
public class PlayerCallBacks implements MediaPlayer.MediaPlayerCallback {

    private MainActivity act;
    MediaPlayer player=null;
    final public static String TAG = "PlayerCallBack";
    
    public Handler handler = new Handler()
    {
        String strText = "Status:";

        @Override
        public void handleMessage(Message msg)
        {
            MediaPlayer.PlayerNotifyCodes status = (MediaPlayer.PlayerNotifyCodes) msg.obj;
            Log.e(TAG, "Notify: " + status);

            switch (status)
            {
                case PLP_TRIAL_VERSION:
                    Toast.makeText(act.getApplicationContext(), "Demo Version!",
                            Toast.LENGTH_SHORT).show();
                    break;

                case CP_CONNECT_STARTING:
                    //player_state = PlayerStates.Busy;
                    player_state_error = PlayerStatesError.None;
                    act.showProgressView(player);
                    break;

                case VRP_NEED_SURFACE:
                    //player_state = PlayerStates.Busy;
                    //showVideoView();
                    //synchronized (waitOnMe) { waitOnMe.notifyAll(); }
                    break;

                case PLP_PLAY_SUCCESSFUL:
                    //player_state = PlayerStates.ReadyForUse;
                    player_state_error = PlayerStatesError.None;
                    act.hideProgressView(player);
                    act.updatePlayerPanelControlButtons(act.isLocked, true, SharedSettings.getInstance().rendererAspectRatioMode);
                    break;

                case PLP_CLOSE_STARTING:
                    //player_state = PlayerStates.Busy;
                    break;

                case PLP_CLOSE_SUCCESSFUL:
                    //player_state = PlayerStates.ReadyForUse;
                    act.hideProgressView(player);
                    act.updatePlayerPanelControlButtons(act.isLocked, false, SharedSettings.getInstance().rendererAspectRatioMode);
                    System.gc();
                    break;

                case PLP_CLOSE_FAILED:
                    //player_state = PlayerStates.ReadyForUse;
                    act.hideProgressView(player);
                    break;

                case CP_CONNECT_FAILED:
                    //player_state = PlayerStates.ReadyForUse;
                    player_state_error = PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;

                case PLP_BUILD_FAILED:
                    //player_state = PlayerStates.ReadyForUse;
                    player_state_error = PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;

                case PLP_PLAY_FAILED:
                    //player_state = PlayerStates.ReadyForUse;
                    player_state_error = PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;

                case PLP_ERROR:
                    //player_state = PlayerStates.ReadyForUse;
                    player_state_error = PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;

                case CP_INTERRUPTED:
                    //player_state = PlayerStates.ReadyForUse;
                    //player_state_error = PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;

                //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
                case CP_STOPPED:
                case VDP_STOPPED:
                case VRP_STOPPED:
                case ADP_STOPPED:
                case ARP_STOPPED:
                    if (!act.isPlayerBusy())
                    {
                        //stopProgressTask();
                        //player_state = PlayerStates.Busy;
                        Log.e(TAG, "AUDIO_RENDERER_PROVIDER_STOPPED_THREAD Close.");
                        player.Close();
                    }
                    break;

                case PLP_EOS:
                    Log.e(TAG, "PLP_EOS: " +act.isFileUrl + ", " + player.getState());
                    if ((act.isFileUrl || act.isModeFile()) && !act.isPlayerBusy() &&
                            player_state_error != PlayerStatesError.Eos)
                    {
                        player_state_error = PlayerStatesError.Eos;
                        if (act.isStartedByIntent)
                        {
                            player.Close();

                            act.onBackPressed();
                            return;
                        }

                        if (!act.mPanelIsVisible)
                        {
                            if (SharedSettings.getInstance().AllowPlayStreamsSequentially) {
                                if(MainActivity.screenMode!= MainActivity.ScreenMode.MultiView)
                                    act.playNextChannelOrBack();
                            }
                            //playNextChannelOrAgain();
                            else
                            {
                                player.Close();
                                act.onBackPressed();
                            }

                            return;
                        }

                        Log.e(TAG, "CONTENT_PROVIDER_ERROR_DISCONNECTED Close.");
                        player.Close();
                    }
                    break;

                case CP_ERROR_DISCONNECTED:
                    if (!act.isPlayerBusy())
                    {
                        if (!act.isFileUrl)
                        {
                            //player_state = PlayerStates.Busy;
                            Log.e(TAG, "CONTENT_PROVIDER_ERROR_DISCONNECTED Close.");
                            player.Close();

                            act.playerConnect(act.m_cur_item);
                            Log.e(TAG, "Reconnecting: " + player.getConfig().getDataReceiveTimeout());

                        }
                    }
                    break;

                default:
            }

            strText += " "+status;
        }
    };

    public enum PlayerStatesError
    {
        None,
        Disconnected,
        Eos
    };
   public PlayerStatesError player_state_error = PlayerStatesError.None;



    public PlayerCallBacks(MainActivity act, MediaPlayer player){
        this.act=act;
        this.player=player;
    }


    @Override
    public int OnReceiveData(ByteBuffer buffer, int size, long pts)
    {
        //Log.i(TAG, "OnReceiveData size: " + size + ", pts: " + pts);
        return 0;
    }

    @Override
    public int Status(int arg0)
    {
        Log.i(TAG, "=Status arg="+arg0);

        MediaPlayer.PlayerNotifyCodes status = MediaPlayer.PlayerNotifyCodes.forValue(arg0);
        if (handler == null || status == null)
            return 0;

        if (player != null)
            Log.i(TAG, "Current state:" + player.getState());

        switch (status)
        {
            // for synchronus process
            //case PLAY_SUCCESSFUL:
//	    	case VRP_NEED_SURFACE:
//	    		synchronized (waitOnMe)
//	    		{
//					Message msg = new Message();
//					msg.obj = status;
//					handler.sendMessage(msg);
//	    		    try
//	    		    {
//	    		        waitOnMe.wait();
//	    		    }
//	    		    catch (InterruptedException e) {}
//	    		}
//				break;

            case CP_CONNECT_FAILED:
            case PLP_BUILD_FAILED:
            case PLP_PLAY_FAILED:
            case PLP_ERROR:
//            case CP_STOPPED:
//            case VDP_STOPPED:
//            case VRP_STOPPED:
//            case ADP_STOPPED:
//            case ARP_STOPPED:
            case CP_ERROR_DISCONNECTED:
            {
                player_state_error = PlayerStatesError.Disconnected;
                Message msg = new Message();
                msg.obj = status;
                msg.what = 1;
                handler.removeMessages(act.mOldMsg);
                act.mOldMsg = msg.what;
                handler.sendMessage(msg);
                break;
            }

            // for asynchronus process
            default:
            {
                Message msg = new Message();
                msg.obj = status;
                msg.what = 1;
                handler.removeMessages(act.mOldMsg);
                act.mOldMsg = msg.what;
                handler.sendMessage(msg);
            }
        }

        return 0;
    }
}
