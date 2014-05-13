LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuild-ui
LOCAL_SRC_FILES := libui.so
include $(PREBUILT_SHARED_LIBRARY)