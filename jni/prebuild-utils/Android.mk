LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuild-libutils
LOCAL_SRC_FILES := libutils.so
include $(PREBUILT_SHARED_LIBRARY)

