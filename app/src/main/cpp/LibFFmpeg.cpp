//
// Created by Zach on 2021/12/15.
//

#include "LibFFmpeg.h"

LibFFmpeg::LibFFmpeg(PlayStatus *playstatus, CallJavaWrapper *callJava, const char *url) {
    this->playStatus = playstatus;
    this->callJava = callJava;
    this->url = url;
}

LibFFmpeg::~LibFFmpeg() {

}


void *decodeFFmpeg(void *data) {
    LibFFmpeg *wlFFmpeg = (LibFFmpeg *) data;
    wlFFmpeg->decodeFFmpegThread();
    pthread_exit(&wlFFmpeg->decodeThread);
}

void LibFFmpeg::prepared() {
    pthread_create(&decodeThread, NULL, decodeFFmpeg, this);
}

void LibFFmpeg::start() {
    if (audio == NULL) {
        if (LOG_DEBUG) {
            LOGE("audio is null");
            return;
        }
    }
    audio->play();

    int count = 0;
    while (playStatus != NULL && !playStatus->exit) {
        // 放入队列
        if (audio->queue->getQueueSize() > 40) {
            continue;
        }

        AVPacket *avPacket = av_packet_alloc();
        if (av_read_frame(pFormatCtx, avPacket) == 0) {
            if (avPacket->stream_index == audio->streamIndex) {
                //解码操作
                count++;
                if (LOG_DEBUG) {
                    LOGE("解码第 %d 帧", count);
                }
                audio->queue->putAvPacket(avPacket);
            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
            }
        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            while (playStatus != NULL && !playStatus->exit) {
                if (audio->queue->getQueueSize() > 0) {
                    continue;
                } else {
                    playStatus->exit = true;
                    break;
                }
            }
        }
    }

    if (LOG_DEBUG) {
        LOGD("解码完成");
    }
}

void LibFFmpeg::decodeFFmpegThread() {
    av_register_all();
    avformat_network_init();
    pFormatCtx = avformat_alloc_context();
    if (avformat_open_input(&pFormatCtx, url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGE("can not open url :%s", url);
        }
        return;
    }
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not find streams from %s", url);
        }
        return;
    }
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) { //得到音频流
            if (audio == NULL) {
                audio = new AudioEngine(playStatus, pFormatCtx->streams[i]->codecpar->sample_rate);
                audio->streamIndex = i;
                audio->codecpar = pFormatCtx->streams[i]->codecpar;
            }
        }
    }

    AVCodec *dec = avcodec_find_decoder(audio->codecpar->codec_id);
    if (!dec) {
        if (LOG_DEBUG) {
            LOGE("can not find decoder");
        }
        return;
    }

    audio->avCodecContext = avcodec_alloc_context3(dec);
    if (!audio->avCodecContext) {
        if (LOG_DEBUG) {
            LOGE("can not alloc new decode ctx");
        }
        return;
    }

    if (avcodec_parameters_to_context(audio->avCodecContext, audio->codecpar) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not fill decode ctx");
        }
        return;
    }

    if (avcodec_open2(audio->avCodecContext, dec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("can not open audio stream");
        }
        return;
    }
    callJava->onCallPrepared(CHILD_THREAD);
}
