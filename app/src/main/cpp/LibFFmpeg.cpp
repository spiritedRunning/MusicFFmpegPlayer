//
// Created by Zach on 2021/12/15.
//

#include "LibFFmpeg.h"

LibFFmpeg::LibFFmpeg(PlayStatus *playstatus, CallJavaWrapper *callJava, const char *url) {
    this->playStatus = playstatus;
    this->callJava = callJava;
    this->url = url;
    pthread_mutex_init(&init_mutex, NULL);
    pthread_mutex_init(&seek_mutex, NULL);
}

LibFFmpeg::~LibFFmpeg() {
    pthread_mutex_destroy(&seek_mutex);
    pthread_mutex_destroy(&init_mutex);
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
        if (playStatus->seek) {  // important!  seek后清空数据，不能播放已清空的数据
            continue;
        }
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
                audio = new AudioEngine(playStatus, pFormatCtx->streams[i]->codecpar->sample_rate,
                                        callJava);
                audio->streamIndex = i;
                audio->codecpar = pFormatCtx->streams[i]->codecpar;

                audio->duration = pFormatCtx->duration / AV_TIME_BASE;  // 总时长
                audio->time_base = pFormatCtx->streams[i]->time_base;
                duration = audio->duration;
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

void LibFFmpeg::seek(int64_t sec) {
    if (duration <= 0) {
        return;
    }
    LOGE("seek start");
    if (sec >= 0 && sec <= duration) {
        if (audio != NULL) {
            playStatus->seek = true;
            audio->queue->clearAvPacket();
            audio->clock = 0;
            audio->last_tick = 0;

            pthread_mutex_lock(&seek_mutex);
            int64_t rel = sec * AV_TIME_BASE;
            avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
            pthread_mutex_unlock(&seek_mutex);

            LOGE("seek finish");
            playStatus->seek = false;
        }
    }
}

void LibFFmpeg::pause() {
    if (audio != NULL) {
        audio->pause();
    }

}

void LibFFmpeg::resume() {
    if (audio != NULL) {
        audio->resume();
    }
}

void LibFFmpeg::setChannel(int channel) {
    if (audio != NULL) {
        audio->setChannel(channel);
    }
}

void LibFFmpeg::setVolume(int vol) {
    if (audio != NULL) {
        audio->setVolume(vol);
    }
}

void LibFFmpeg::setSpeed(float speed) {
    if (audio != NULL) {
        audio->setSpeed(speed);
    }
}

void LibFFmpeg::setPitch(float pitch) {
    if (audio != NULL) {
        audio->setPitch(pitch);
    }
}

void LibFFmpeg::release() {
    if (LOG_DEBUG) {
        LOGE("开始释放ffmpeg");
    }
    playStatus->exit = true;

    int sleepCount = 0;
    pthread_mutex_lock(&init_mutex);
    while (!exit) {
        if (sleepCount > 1000) {
            exit = true;
        }
        if (LOG_DEBUG) {
            LOGE("wait ffmpeg  exit %d", sleepCount);
        }
        sleepCount++;
        av_usleep(1000 * 10); //暂停10毫秒
    }

    if (audio != NULL) {
        audio->release();
        delete (audio);
        audio = NULL;
    }

    if (LOG_DEBUG) {
        LOGE("释放 封装格式上下文");
    }
    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }
    if (LOG_DEBUG) {
        LOGE("释放 callJava");
    }
    if (callJava != NULL) {
        callJava = NULL;
    }
    if (LOG_DEBUG) {
        LOGE("释放 playstatus");
    }
    if (playStatus != NULL) {
        playStatus = NULL;
    }
    pthread_mutex_unlock(&init_mutex);

}
