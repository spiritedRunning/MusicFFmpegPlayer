#include <jni.h>
#include <string>
#include "CallJavaWrapper.h"
#include "LibFFmpeg.h"
#include "PlayStatus.h"

_JavaVM *javaVM = NULL;
CallJavaWrapper *callJava = NULL;
LibFFmpeg *ffmpeg = NULL;
PlayStatus *playstatus = NULL;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_musicffmpegplayer_player_NPlayerInterface_n_1prepared(JNIEnv *env, jobject thiz,
                                                                       jstring source_) {
    const char *source = env->GetStringUTFChars(source_, 0);

    if (ffmpeg == NULL) {
        if (callJava == NULL) {
            callJava = new CallJavaWrapper(javaVM, env, &thiz);
        }
        playstatus = new PlayStatus();
        ffmpeg = new LibFFmpeg(playstatus, callJava, source);
        ffmpeg->prepared();
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_musicffmpegplayer_player_NPlayerInterface_n_1start(JNIEnv *env, jobject thiz) {
    if (ffmpeg != NULL) {
        ffmpeg->start();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_musicffmpegplayer_player_NPlayerInterface_n_1seek(JNIEnv *env, jobject thiz,
                                                                   jint sec) {
    LOGE("seek progress: %d", sec);
    if (ffmpeg != NULL) {
        ffmpeg->seek(sec);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_musicffmpegplayer_player_NPlayerInterface_n_1resume(JNIEnv *env, jobject thiz) {
    if (ffmpeg != NULL) {
        ffmpeg->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_musicffmpegplayer_player_NPlayerInterface_n_1pause(JNIEnv *env, jobject thiz) {
    if (ffmpeg != NULL) {
        ffmpeg->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_musicffmpegplayer_player_NPlayerInterface_n_1channel(JNIEnv *env, jobject thiz,
                                                                      jint channel) {
    if (ffmpeg != NULL) {
        ffmpeg->setChannel(channel);
    }
}