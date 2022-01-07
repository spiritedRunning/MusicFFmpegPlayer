//
// Created by Zach on 2021/12/15.
//

#ifndef MUSICFFMPEGPLAYER_LIBFFMPEG_H
#define MUSICFFMPEGPLAYER_LIBFFMPEG_H

#include "PlayStatus.h"
#include "AudioEngine.h"
#include "Log.h"
#include "CallJavaWrapper.h"


extern "C"{
#include "libavformat/avformat.h"
#include "libavutil/time.h"
}

class LibFFmpeg {
public:
    CallJavaWrapper *callJava = NULL;
    const char *url = NULL;
    pthread_t decodeThread;
    AVFormatContext *pFormatCtx = NULL;
    AudioEngine *audio = NULL;
    PlayStatus *playStatus = NULL;

    int duration = 0;
    pthread_mutex_t seek_mutex;
    pthread_mutex_t init_mutex;

    bool exit = false;

public:
    LibFFmpeg(PlayStatus *playstatus, CallJavaWrapper *callJava, const char *url);
    ~LibFFmpeg();

    void prepared();
    void start();
    void pause();
    void seek(int64_t sec);
    void resume();
    void setChannel(int channel);
    void setVolume(int vol);

    void setSpeed(float speed);
    void setPitch(float pitch);

    void decodeFFmpegThread();

    void release();
};


#endif //MUSICFFMPEGPLAYER_LIBFFMPEG_H
