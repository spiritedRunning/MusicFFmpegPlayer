cmake_minimum_required(VERSION 3.4.1)

include_directories(include)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -L${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI} ")

message(-------------)
message(${CMAKE_SOURCE_DIR})


aux_source_directory(. SOURCE)

add_library(
        native-lib
        SHARED
        ${SOURCE})


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
        log)