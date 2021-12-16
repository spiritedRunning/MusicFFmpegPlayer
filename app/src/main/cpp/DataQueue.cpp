//
// Created by Zach on 2021/12/15.
//

#include "DataQueue.h"

DataQueue::DataQueue(PlayStatus *playStatus) {
    this->playStatus = playStatus;
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);
}

DataQueue::~DataQueue() {
    clearAvPacket();
}

int DataQueue::putAvPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);

    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

int DataQueue::getAvPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);

    while (playStatus != NULL && !playStatus->exit) {
        if (queuePacket.size() > 0) {
            AVPacket *avPacket = queuePacket.front();
            if (av_packet_ref(packet, avPacket) == 0) {
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            break;
        } else {
            pthread_cond_wait(&condPacket, &mutexPacket);
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int DataQueue::getQueueSize() {
    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);

    return size;
}

void DataQueue::clearAvPacket() {
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    while (!queuePacket.empty()) {
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = NULL;
    }
    pthread_mutex_unlock(&mutexPacket);
}
