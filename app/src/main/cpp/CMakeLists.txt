cmake_minimum_required(VERSION 3.4.1)

include_directories(include)
include_directories(soundtouch/include)
include_directories(soundtouch/SoundTouch)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -L${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI} ")

message(-------------)
message(${CMAKE_SOURCE_DIR})

aux_source_directory(. SOURCE)
aux_source_directory(./soundtouch/SoundTouch SouchSource)

add_library(
        native-lib
        SHARED
        ${SOURCE}
        ${SouchSource})


target_link_libraries(
        native-lib
        avcodec
        avdevice
        avfilter
        avformat
        avutil
        swresample
        swscale
        OpenSLES
        log
        android)