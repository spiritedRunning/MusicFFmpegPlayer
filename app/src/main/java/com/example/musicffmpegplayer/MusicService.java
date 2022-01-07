package com.example.musicffmpegplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicffmpegplayer.listener.IPlayerListener;
import com.example.musicffmpegplayer.listener.WlOnPreparedListener;
import com.example.musicffmpegplayer.musicui.model.MusicData;
import com.example.musicffmpegplayer.player.NPlayerInterface;

import java.util.List;

/**
 * Created by Zach on 2021/12/15 16:44
 */
public class MusicService extends Service implements IPlayerListener {

    private static final String TAG = "MusicService";

    private NPlayerInterface nPlayerInterface;

    /*操作指令*/
    public static final String ACTION_OPT_MUSIC_PLAY = "ACTION_OPT_MUSIC_PLAY";
    public static final String ACTION_OPT_MUSIC_PAUSE = "ACTION_OPT_MUSIC_PAUSE";
    public static final String ACTION_OPT_MUSIC_RESUME = "ACTION_OPT_MUSIC_RESUME";
    public static final String ACTION_OPT_MUSIC_NEXT = "ACTION_OPT_MUSIC_NEXT";
    public static final String ACTION_OPT_MUSIC_LAST = "ACTION_OPT_MUSIC_LAST";
    public static final String ACTION_OPT_MUSIC_SEEK_TO = "ACTION_OPT_MUSIC_SEEK_TO";
    public static final String ACTION_OPT_MUSIC_LEFT = "ACTION_OPT_MUSIC_LEFT";
    public static final String ACTION_OPT_MUSIC_RIGHT = "ACTION_OPT_MUSIC_RIGHT";
    public static final String ACTION_OPT_MUSIC_CENTER = "ACTION_OPT_MUSIC_CENTER";
    public static final String ACTION_OPT_MUSIC_VOLUME = "ACTION_OPT_MUSIC_VOLUME";

    public static final String ACTION_OPT_MUSIC_SPEED_AND_NO_PITCH = "ACTION_OPT_MUSIC_SPEED_AN_NO_PITCH";
    public static final String ACTION_OPT_MUSIC_NO_SPEED_AND_PITCH = "ACTION_OPT_MUSIC_SPEED_NO_AN_PITCH";
    public static final String ACTION_OPT_MUSIC_SPEED_AND_PITCH = "ACTION_OPT_MUSIC_SPEED_AN_PITCH";
    public static final String ACTION_OPT_MUSIC_SPEED_PITCH_NORMAL = "ACTION_OPT_MUSIC_SPEED_PITCH_NOMAORL";


    /*状态指令*/
    public static final String ACTION_STATUS_MUSIC_PLAY = "ACTION_STATUS_MUSIC_PLAY";
    public static final String ACTION_STATUS_MUSIC_PAUSE = "ACTION_STATUS_MUSIC_PAUSE";
    public static final String ACTION_STATUS_MUSIC_COMPLETE = "ACTION_STATUS_MUSIC_COMPLETE";
    public static final String ACTION_STATUS_MUSIC_DURATION = "ACTION_STATUS_MUSIC_DURATION";
    public static final String ACTION_STATUS_MUSIC_PLAYER_TIME = "ACTION_STATUS_MUSIC_PLAYER_TIME";
    public static final String PARAM_MUSIC_DURATION = "PARAM_MUSIC_DURATION";
    public static final String PARAM_MUSIC_SEEK_TO = "PARAM_MUSIC_SEEK_TO";
    public static final String PARAM_MUSIC_CURRENT_POSITION = "PARAM_MUSIC_CURRENT_POSITION";
    public static final String PARAM_MUSIC_IS_OVER = "PARAM_MUSIC_IS_OVER";

    private int mCurrentMusicIndex = 0;

    List<MusicData> musicDataList;
    private MusicReceiver mMusicReceiver = new MusicReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initBoardCastReceiver();
        nPlayerInterface = new NPlayerInterface();
        nPlayerInterface.setPlayerListener(this);
        nPlayerInterface.setWlOnPreparedListener(new WlOnPreparedListener() {
            @Override
            public void onPrepared() {
                Log.d(TAG, "准备好了，可以开始播放声音了");
                nPlayerInterface.start();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        musicDataList = (List<MusicData>) intent.getSerializableExtra(MainActivity.PARAM_MUSIC_LIST);
        return START_STICKY;
    }

    private void initBoardCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_OPT_MUSIC_PLAY);
        intentFilter.addAction(ACTION_OPT_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_OPT_MUSIC_RESUME);
        intentFilter.addAction(ACTION_OPT_MUSIC_NEXT);
        intentFilter.addAction(ACTION_OPT_MUSIC_LAST);
        intentFilter.addAction(ACTION_OPT_MUSIC_SEEK_TO);
        intentFilter.addAction(ACTION_OPT_MUSIC_LEFT);
        intentFilter.addAction(ACTION_OPT_MUSIC_RIGHT);
        intentFilter.addAction(ACTION_OPT_MUSIC_VOLUME);
        intentFilter.addAction(ACTION_OPT_MUSIC_CENTER);

        intentFilter.addAction(ACTION_OPT_MUSIC_SPEED_AND_NO_PITCH);
        intentFilter.addAction(ACTION_OPT_MUSIC_NO_SPEED_AND_PITCH);
        intentFilter.addAction(ACTION_OPT_MUSIC_SPEED_AND_PITCH);
        intentFilter.addAction(ACTION_OPT_MUSIC_SPEED_PITCH_NORMAL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, intentFilter);
    }


    @Override
    public void onLoad(boolean load) {

    }

    @Override
    public void onCurrentTime(int currentTime, int totalTime) {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_PLAYER_TIME);
        intent.putExtra("currentTime", currentTime);
        intent.putExtra("totalTime", totalTime);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onError(int code, String msg) {

    }

    @Override
    public void onPause(boolean pause) {

    }

    @Override
    public void onDbValue(int db) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public String onNext() {
        return null;
    }


    class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: " + action);
            if (action.equals(ACTION_OPT_MUSIC_PLAY)) {
                play(mCurrentMusicIndex);
            } else if (action.equals(ACTION_OPT_MUSIC_LAST)) {
                last();
            } else if (action.equals(ACTION_OPT_MUSIC_NEXT)) {
                next();
            } else if (action.equals(ACTION_OPT_MUSIC_SEEK_TO)) {
                int position = intent.getIntExtra(MusicService.PARAM_MUSIC_SEEK_TO, 0);
                seekTo(position);
            } else if (action.equals(ACTION_OPT_MUSIC_RESUME)) {
                resume();
            } else if (action.equals(ACTION_OPT_MUSIC_PAUSE)) {
                pause();
            } else if (action.equals(ACTION_OPT_MUSIC_RIGHT)) {
                nPlayerInterface.setChannel(0);
            } else if (action.equals(ACTION_OPT_MUSIC_LEFT)) {
                nPlayerInterface.setChannel(1);
            } else if (action.equals(ACTION_OPT_MUSIC_CENTER)) {
                nPlayerInterface.setChannel(2);
            } else if (action.equals(ACTION_OPT_MUSIC_VOLUME)) {
                int vol = intent.getIntExtra("VOLUME", 20);
                nPlayerInterface.setVolume(vol);
            } else if (action.equals(MusicService.ACTION_OPT_MUSIC_SPEED_AND_NO_PITCH)) {
                nPlayerInterface.setSpeed(1.5f);
                nPlayerInterface.setPitch(1.0f);
            } else if (action.equals(MusicService.ACTION_OPT_MUSIC_NO_SPEED_AND_PITCH)) {
                nPlayerInterface.setSpeed(1.0f);
                nPlayerInterface.setPitch(1.5f);
            } else if (action.equals(MusicService.ACTION_OPT_MUSIC_SPEED_AND_PITCH)) {
                nPlayerInterface.setSpeed(1.5f);
                nPlayerInterface.setPitch(1.5f);
            } else if (action.equals(MusicService.ACTION_OPT_MUSIC_SPEED_PITCH_NORMAL)) {
                nPlayerInterface.setSpeed(1.0f);
                nPlayerInterface.setPitch(1.0f);
            }
        }
    }

    private void play(final int index) {
        nPlayerInterface.setSource("http://mn.maliuedu.com/music/dengniguilai.mp3");
        nPlayerInterface.prepared();
    }

    private void pause() {
        nPlayerInterface.pause();
    }

    private void resume() {
        nPlayerInterface.resume();
    }


    private void stop() {
    }

    private void next() {
    }

    private void last() {

    }

    private void seekTo(int position) {
        nPlayerInterface.seek(position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
    }
}
