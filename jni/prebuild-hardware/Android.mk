LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuild-hardware
LOCAL_SRC_FILES := libhardware.so
include $(PREBUILT_SHARED_LIBRARY)