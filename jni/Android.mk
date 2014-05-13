LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := nativegl
LOCAL_SRC_FILES := NativeInterface.cpp
				  
LOCAL_LDLIBS += -llog -lEGL -lGLESv2 -landroid
				 					
LOCAL_C_INCLUDES +=	jni/gui \
					jni/utils \
					jni/cutils \
					jni/hardware\
					jni/system\
							
LOCAL_SHARED_LIBRARIES += prebuild-libgui \
						  prebuild-libutils \
						  prebuild-libcutils \
						  prebuild-hardware \
						  prebuild-ui \
						  					              		 
LOCAL_CFLAGS += -Wno-multichar -DHAVE_PTHREADS -fpermissive -DGL_GLEXT_PROTOTYPES=1 -DEGL_EGLEXT_PROTOTYPES=1
LOCAL_CERTIFICATE := platform

include $(BUILD_SHARED_LIBRARY)
include $(call all-makefiles-under,$(LOCAL_PATH))
