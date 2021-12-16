//
// Created by Zach on 2021/12/15.
//

#ifndef MUSICFFMPEGPLAYER_CALLJAVAWRAPPER_H
#define MUSICFFMPEGPLAYER_CALLJAVAWRAPPER_H

#include "jni.h"
#include <linux/stddef.h>
#include "Log.h"

#define MAIN_THREAD 0
#define CHILD_THREAD 1


class CallJavaWrapper {
public:
    _JavaVM *javaVM = NULL;
    JNIEnv *jniEnv = NULL;
    jobject jobj;

    jmethodID jmid_prepared;
    jmethodID jmid_timeinfo;

    CallJavaWrapper(_JavaVM *javaVM, JNIEnv *env, jobject *obj);
    ~CallJavaWrapper();

    void onCallPrepared(int type);

};


#endif //MUSICFFMPEGPLAYER_CALLJAVAWRAPPER_H
