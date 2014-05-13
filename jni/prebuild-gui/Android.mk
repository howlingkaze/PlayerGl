LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuild-libgui
LOCAL_SRC_FILES := libgui.so
include $(PREBUILT_SHARED_LIBRARY)

