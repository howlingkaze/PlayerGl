#include <jni.h>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <ui/GraphicBuffer.h>

using namespace android;

#define TAG "Native GLES"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
//----------------native methods

extern "C"
{


GraphicBuffer *window;
EGLImageKHR image;
EGLDisplay DisplayTarget;
ANativeWindow* nativewindow;
int nWidth;
int nHeight;
bool VERBOSE;
static jfieldID gDisplay_EGLDisplayFieldID;
GLubyte* pixels;
bool PixelTempAllocated;
GLuint nativeTextureID;
GLubyte* Glpixel;

GraphicBuffer* pixmap; // for text the eglcreatebitmapfrom

static inline EGLDisplay getDisplay(JNIEnv* env, jobject o) {
    if (!o) return EGL_NO_DISPLAY;
    return (EGLDisplay)env->GetIntField(o, gDisplay_EGLDisplayFieldID);
}



static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGE("GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
    	LOGE("after %s() glError (0x%x)\n", op, error);
    }
}


void Native_getEglDisplay(JNIEnv *env, jclass thiz,jobject display)
{
	//reimplement the jni method of android opengl implementation,the result should be the same as eglGetCurrentDisplay().

	 jclass display_class = env->FindClass("com/google/android/gles_jni/EGLDisplayImpl");
	 gDisplay_EGLDisplayFieldID = env->GetFieldID(display_class, "mEGLDisplay", "I");
	 DisplayTarget= getDisplay(env, display);
}


void NativeDraw(JNIEnv *env, jclass thiz)
{
	glReadPixels(0, 0, nWidth, nHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

	ANativeWindow_Buffer nbuffer;
	if (ANativeWindow_lock(nativewindow, &nbuffer, NULL) == 0) {
	  memcpy(nbuffer.bits, pixels,  nWidth * nHeight* 4 );
	  ANativeWindow_unlockAndPost(nativewindow);
	}
}


void Native_getSurface(JNIEnv *env, jclass thiz, jobject jsurface)
{
	nativewindow = ANativeWindow_fromSurface(env, jsurface);
	LOGV("ANativeWindow_fromSurface");

	//nWidth = ANativeWindow_getWidth(nativewindow);
    //nHeight = ANativeWindow_getHeight(nativewindow);

   nWidth = 1280;
   nHeight = 720;



	LOGV("Get surfcae width :%d ,height :%d",nWidth,nHeight);


    if(PixelTempAllocated!=1)
    {
    	pixels = new GLubyte[nWidth * nHeight * 4];
    	PixelTempAllocated=1;
    }

	ANativeWindow_setBuffersGeometry(nativewindow,nWidth,nHeight,WINDOW_FORMAT_RGBA_8888);
	//Set outputbuffer size, should be matched with the video size.


	//Glpixel =new GLubyte[nWidth * nHeight * 4];

	char* temp = new GLubyte[nWidth * nHeight* 4];

	memset (temp,255,nWidth * nHeight* 4);
	memset (pixels,128,nWidth * nHeight* 3);

	ANativeWindow_Buffer nbuffer;
	if (ANativeWindow_lock(nativewindow, &nbuffer, NULL) == 0) {
	  memcpy(nbuffer.bits, temp,  nWidth * nHeight* 4 );
	  ANativeWindow_unlockAndPost(nativewindow);
	}

	LOGV("Pre render done");


	delete []temp;

}

void NativeInit()
{
	window=NULL;
	image=NULL;
	VERBOSE=0;
	PixelTempAllocated=0;

}

void NativeRelease()
{
	ANativeWindow_release(nativewindow);
	delete []pixels;
}


jint NativeGetOutputTexture(JNIEnv *env, jclass thiz,jint width,jint height)
{

	LOGV("start set up Native Texture");
	window = new GraphicBuffer(width, height, PIXEL_FORMAT_RGBA_8888, GraphicBuffer::USAGE_SW_READ_OFTEN | GraphicBuffer::USAGE_HW_TEXTURE);

	ANativeWindowBuffer* buffer = window->getNativeBuffer();

	EGLint eglImageAttributes[] = {EGL_WIDTH, width,
									   EGL_HEIGHT, height,
									   EGL_MATCH_FORMAT_KHR,EGL_FORMAT_RGBA_8888_KHR,
									   EGL_IMAGE_PRESERVED_KHR, 1,
									   EGL_NONE
									};

	EGLint attrs[] = {
	        EGL_IMAGE_PRESERVED_KHR, EGL_TRUE,
	        EGL_NONE,
	    };


	image = eglCreateImageKHR(eglGetCurrentDisplay(),EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID , reinterpret_cast<EGLClientBuffer>(buffer), attrs);
	checkGlError("eglCreateImageKHRW");
	if (image == EGL_NO_IMAGE_KHR )
	{
		LOGE("Fail to create ImageHKHR");
		return 0;
	}
	glGenTextures(1, &nativeTextureID);
    checkGlError("glGenTextures");

	return nativeTextureID;
}


void NativeSetup()
{
	LOGV("--In NaitveSetup--");


}
void NativesetFixedScale(int width,int height)
{

}

//----------------------------------jni_register part-----------------------------------------------------


#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

#define JNIREG_CLASS "nsysu/ee/mmlab/playergl/OutputSurface"

static JNINativeMethod method_table[] = {
    { "NativeSetup", "()V", (void*)NativeSetup},
    { "setSurface", "(Landroid/view/Surface;)V", (void*)Native_getSurface},
    { "NaitveSourceInit", "()V", (void*)NativeInit},
    { "NaitveSourceRelease", "()V", (void*)NativeRelease},
    { "NaitveGetTexture", "(II)I", (void*)NativeGetOutputTexture},
    { "setFixedScale", "(II)V", (void*)NativesetFixedScale},
    { "NativeDrawFrame", "()V", (void*)NativeDraw}
};



static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;
    clazz = (env)->FindClass(className);

    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((env)->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

int register_ndk_load(JNIEnv *env)
{
    return registerNativeMethods(env, JNIREG_CLASS,
            method_table, NELEM(method_table));
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	LOGV("JNI onload");
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }
    register_ndk_load(env);

    return JNI_VERSION_1_4;
}


}


