//
// Created by Zach on 2021/12/15.
//

#ifndef MUSICFFMPEGPLAYER_DATAQUEUE_H
#define MUSICFFMPEGPLAYER_DATAQUEUE_H

#include "queue"
#include "pthread.h"
#include "PlayStatus.h"

extern "C" {
#include "libavcodec/avcodec.h"
};

using namespace std;

class DataQueue {
public:
    queue<AVPacket *> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;
    PlayStatus *playStatus = NULL;


    DataQueue(PlayStatus *playStatus);
    ~DataQueue();

    int putAvPacket(AVPacket *packet);
    int getAvPacket(AVPacket *packet);

    int getQueueSize();

    void clearAvPacket();

};


#endif //MUSICFFMPEGPLAYER_DATAQUEUE_H
