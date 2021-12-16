//
// Created by Zach on 2021/12/15.
//

#ifndef MUSICFFMPEGPLAYER_LOG_H
#define MUSICFFMPEGPLAYER_LOG_H

#include "android/log.h"

#define LOG_DEBUG true
#define TAG "MusicFFmpegPlayer"

#define LOGD(FORMAT, ...) __android_log_print(ANDROID_LOG_DEBUG,TAG,FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);


#endif //MUSICFFMPEGPLAYER_LOG_H
