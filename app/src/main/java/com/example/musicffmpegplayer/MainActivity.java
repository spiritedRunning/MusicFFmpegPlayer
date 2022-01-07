package com.example.musicffmpegplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicffmpegplayer.musicui.model.MusicData;
import com.example.musicffmpegplayer.musicui.utils.DisplayUtil;
import com.example.musicffmpegplayer.musicui.widget.BackgourndAnimationRelativeLayout;
import com.example.musicffmpegplayer.musicui.widget.DiscView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DiscView.IPlayInfo, View.OnClickListener {
    private static final String TAG = "MainActivity";

    private DiscView mDisc;
    private Toolbar mToolbar;
    private SeekBar mSeekBar;
    private ImageView mIvPlayOrPause, mIvNext, mIvLast, mIvStop;
    private TextView mTvMusicDuration, mTvTotalMusicDuration;
    private BackgourndAnimationRelativeLayout mRootLayout;
    public static final int MUSIC_MESSAGE = 0;

    private SeekBar mVolumeBar;

    public static final String PARAM_MUSIC_LIST = "PARAM_MUSIC_LIST";
    DisplayUtil displayUtil = new DisplayUtil();
    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private List<MusicData> mMusicDatas = new ArrayList<>();
    private int totalTime;
    private int position;
    private boolean playState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMusicDatas();
        initView();
        initMusicReceiver();
        DisplayUtil.makeStatusBarTransparent(this);
    }

    private void initMusicDatas() {
        MusicData musicData1 = new MusicData(R.raw.music1, R.raw.ic_music1, "等你归来", "程响");
        MusicData musicData2 = new MusicData(R.raw.music2, R.raw.ic_music2, "Nightingale", "YANI");
        MusicData musicData3 = new MusicData(R.raw.music3, R.raw.ic_music3, "Cornfield Chase", "Hans Zimmer");
        mMusicDatas.add(musicData1);
        mMusicDatas.add(musicData2);
        mMusicDatas.add(musicData3);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(PARAM_MUSIC_LIST, (Serializable) mMusicDatas);
        startService(intent);
    }

    private void initView() {
        mDisc = findViewById(R.id.discview);
        mIvNext = findViewById(R.id.ivNext);
        mIvLast = findViewById(R.id.ivLast);
        mIvStop = findViewById(R.id.ivStop);
        mIvPlayOrPause = findViewById(R.id.ivPlayOrPause);
        mTvMusicDuration = findViewById(R.id.tvCurrentTime);
        mTvTotalMusicDuration = findViewById(R.id.tvTotalTime);

        mSeekBar = findViewById(R.id.musicSeekBar);
        mRootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);
        mVolumeBar = findViewById(R.id.volumeBar);


        mIvStop.setOnClickListener(this);
        mDisc.setPlayInfoListener(this);
        mIvLast.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPlayOrPause.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position = totalTime * progress / 100;
                mTvMusicDuration.setText(displayUtil.duration2Time(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "onStopTrackingTouch: " + position);
                seekTo(position);
            }
        });

        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "vol progress: " + progress);

                Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_VOLUME);
                intent.putExtra("VOLUME", progress);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTvMusicDuration.setText(displayUtil.duration2Time(0));
        mTvTotalMusicDuration.setText(displayUtil.duration2Time(0));
        mDisc.setMusicDataList(mMusicDatas);
    }

    private void initMusicReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAYER_TIME);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        if (v == mIvPlayOrPause) {
            Log.i(TAG, "onClick: ---------" + playState);

            if (playState) {
                mIvPlayOrPause.setImageResource(R.mipmap.ic_play);
                pause();
                mDisc.pause();
            } else {
                mIvPlayOrPause.setImageResource(R.mipmap.ic_pause);
                resume();
                mDisc.play();
            }

            playState = !playState;
        } else if (v == mIvNext) {
            mDisc.next();
        } else if (v == mIvLast) {
            mDisc.last();
        } else if (v == mIvStop) {
            stop();
        }

    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {
        getSupportActionBar().setTitle(musicName);
        getSupportActionBar().setSubtitle(musicAuthor);
    }

    @Override
    public void onMusicPicChanged(int musicPicRes) {
        displayUtil.try2UpdateMusicPicBackground(this, mRootLayout, musicPicRes);
    }

    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
        switch (musicChangedStatus) {
            case PLAY: {
                play();
                break;
            }
            case PAUSE: {
                pause();
                break;
            }
            case NEXT: {
                next();
                break;
            }
            case LAST: {
                last();
                break;
            }
            case STOP: {
                stop();
                break;
            }
        }
    }

    private void play() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
    }

    private void pause() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
    }

    public void resume() {
        optMusic(MusicService.ACTION_OPT_MUSIC_RESUME);
    }

    private void stop() {
        mIvPlayOrPause.setImageResource(R.mipmap.ic_play);
        mTvMusicDuration.setText(displayUtil.duration2Time(0));
        mTvTotalMusicDuration.setText(displayUtil.duration2Time(0));
        mSeekBar.setProgress(0);
        mDisc.pause();

        optMusic(MusicService.ACTION_OPT_MUSIC_STOP);
    }

    private void next() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        }, DiscView.DURATION_NEEDLE_ANIAMTOR);
        mTvMusicDuration.setText(displayUtil.duration2Time(0));
        mTvTotalMusicDuration.setText(displayUtil.duration2Time(0));
    }

    private void last() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
            }
        }, DiscView.DURATION_NEEDLE_ANIAMTOR);

        mTvMusicDuration.setText(displayUtil.duration2Time(0));
        mTvTotalMusicDuration.setText(displayUtil.duration2Time(0));
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO, position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void left(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_LEFT);
    }

    public void right(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_RIGHT);
    }

    public void center(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_CENTER);
    }


    public void OnSpeed(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_SPEED_AND_NO_PITCH);
    }

    public void OnPitch(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_NO_SPEED_AND_PITCH);
    }

    public void OnSpeedPitch(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_SPEED_AND_PITCH);
    }

    public void OnNormalPlay(View view) {
        optMusic(MusicService.ACTION_OPT_MUSIC_SPEED_PITCH_NORMAL);
    }

    private void optMusic(final String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
    }


    private void playCurrentTime(int currentTime, int totalTime) {
        mSeekBar.setProgress(currentTime * 100 / totalTime);
        this.totalTime = totalTime;
        mTvMusicDuration.setText(DisplayUtil.secdsToDateFormat(currentTime, totalTime));
        mTvTotalMusicDuration.setText(DisplayUtil.secdsToDateFormat(totalTime, totalTime));
    }

    private void complete(boolean isOver) {
        if (isOver) {
            mDisc.stop();
        } else {
            mDisc.next();
        }
    }


    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                mIvPlayOrPause.setImageResource(R.mipmap.ic_pause);
                int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                mSeekBar.setProgress(currentPosition);
                if (!mDisc.isPlaying()) {
                    mDisc.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                mIvPlayOrPause.setImageResource(R.mipmap.ic_play);
                if (mDisc.isPlaying()) {
                    mDisc.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_DURATION)) {
                int duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
//                updateMusicDurationInfo(duration);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_COMPLETE)) {
                boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
                complete(isOver);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAYER_TIME)) {
                int currentTime = intent.getIntExtra("currentTime", 0);
                int totalTime = intent.getIntExtra("totalTime", 0);
                playCurrentTime(currentTime, totalTime);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
    }

}
