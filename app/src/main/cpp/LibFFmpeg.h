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
}

class LibFFmpeg {
public:
    CallJavaWrapper *callJava = NULL;
    const char *url = NULL;
    pthread_t decodeThread;
    AVFormatContext *pFormatCtx = NULL;
    AudioEngine *audio = NULL;
    PlayStatus *playStatus = NULL;

public:
    LibFFmpeg(PlayStatus *playstatus, CallJavaWrapper *callJava, const char *url);
    ~LibFFmpeg();

    void prepared();
    void start();


    void decodeFFmpegThread();

};


#endif //MUSICFFMPEGPLAYER_LIBFFMPEG_H
