package com.example.musicffmpegplayer.player;

import android.text.TextUtils;
import android.util.Log;

import com.example.musicffmpegplayer.listener.IPlayerListener;
import com.example.musicffmpegplayer.listener.WlOnPreparedListener;

/**
 * Created by Zach on 2021/12/15 16:41
 * <p>
 * java与native的互相回调接口
 */
public class NPlayerInterface {
    private static final String TAG = "NPlayerInterface";

    static {
        System.loadLibrary("native-lib");
    }

    private String source;//数据源
    private WlOnPreparedListener wlOnPreparedListener;

    private IPlayerListener playerListener;

    public void setPlayerListener(IPlayerListener playerListener) {
        this.playerListener = playerListener;
    }

    public NPlayerInterface() {
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setWlOnPreparedListener(WlOnPreparedListener wlOnPreparedListener) {
        this.wlOnPreparedListener = wlOnPreparedListener;
    }


    public void prepared() {
        if (TextUtils.isEmpty(source)) {
            Log.d(TAG, "source not be empty");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_prepared(source);
            }
        }).start();

    }

    public void start() {
        if (TextUtils.isEmpty(source)) {
            Log.d(TAG, "source is empty");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_start();
            }
        }).start();
    }


    public void pause() {
        n_pause();
    }

    public void resume() {
        n_resume();
    }

    public void setChannel(int channel) {
        n_channel(channel);
    }


    public void seek(int sec) {
        n_seek(sec);
    }


    public native void n_prepared(String source);

    public native void n_start();

    private native void n_seek(int sec);

    private native void n_resume();
    private native void n_pause();
    private native void n_channel(int channel);

    /*******************************************************************************************/
    /***** C++ 回调Java start ****/
    public void onCallPrepared() {
        if (wlOnPreparedListener != null) {
            wlOnPreparedListener.onPrepared();
        }
    }

    public void onCallTimeInfo(int currentTime, int totalTime) {
        if (playerListener == null) {
            return;
        }
        playerListener.onCurrentTime(currentTime, totalTime);
    }



}
