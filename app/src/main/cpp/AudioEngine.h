//
// Created by Zach on 2021/12/15.
//

#ifndef MUSICFFMPEGPLAYER_AUDIOENGINE_H
#define MUSICFFMPEGPLAYER_AUDIOENGINE_H

#include "PlayStatus.h"
#include "CallJavaWrapper.h"
#include "DataQueue.h"
#include "SoundTouch.h"


extern "C" {
#include "libavcodec/avcodec.h"
#include <libswresample/swresample.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

using namespace soundtouch;

class AudioEngine {
public:
    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecpar = NULL;
    DataQueue *queue = NULL;
    PlayStatus *playstatus = NULL;

    pthread_t thread_play;
    AVPacket *avPacket = NULL;
    AVFrame *avFrame = NULL;
    int ret = 0;
    uint8_t *buffer = NULL;
    int data_size = 0;
    int sample_rate = 0;

    // 引擎接口
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    //混音器
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //pcm
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    SLVolumeItf pcmVolumePlay = NULL;
    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;


    CallJavaWrapper *callJava = NULL;
    int duration = 0;
    AVRational time_base;
    double now_time;  // 当前frame时间
    double clock;  // 当前播放时间， 准确时间

    double last_tick; // 上次调用时间

    int mute = 2;   // 立体声
    SLMuteSoloItf pcmMutePlay = NULL;   // 声道

    float speed = 1.0f;
    float pitch = 1.0f;
    SoundTouch *soundTouch = NULL;
    uint8_t *out_buffer = NULL;
    bool finished = true;
    SAMPLETYPE *sampleBuffer = NULL;
    int nb = 0;  // 转换之后Pcm数据多少
    int num = 0; // 新波的个数

public:
    AudioEngine(PlayStatus *playStatus, int sample_rate, CallJavaWrapper *callJava);

    ~AudioEngine();

    void play();
    void pause();
    void resume();
    void setChannel(int channel);
    void setVolume(int vol);

    void setSpeed(float speed);
    void setPitch(float pitch);
    int getSoundTouchData();

    int resampleAudio(void **pcmbuf);

    void initOpenSLES();

    int getCurrentSampleRateForOpensles(int sample_rate);

    void release();
};


#endif //MUSICFFMPEGPLAYER_AUDIOENGINE_H
