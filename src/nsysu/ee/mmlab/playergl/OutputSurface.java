package nsysu.ee.mmlab.playergl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class OutputSurface implements SurfaceTexture.OnFrameAvailableListener {
	
    private Gl20TextureRender mTextureRender;
    
    private SurfaceTexture mSurfaceTexture;
    // Output Surface for the MediaDecoder
    
    private Surface mSurface;
      
    private SurfaceTexture OutputTexture;
    
    private static final boolean VERBOSE = true;
	// true to trigger log output.
    
    private String TAG="Output Surface";

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    int mWidth;
    int mHeight;
    
    Bitmap pixels; // for software decoding or eglpixmapSurface

    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;
    private boolean isNativeMethod=true;

    private ByteBuffer mPixelBuf;                       // used by saveFrame()
    private int[] mColorSwapBuf;                        // used by saveFrame()
    private TextureView mOutputTexture;
    private SurfaceHolder mOutputHolder;
    int mTextureId;
    
    

    /**
     * Creates a OutputSurface backed by a pbuffer with the specified dimensions.  The
     * new EGL context and surface will be made current.  Creates a Surface that can be passed
     * to MediaCodec.configure().
     */
    public OutputSurface(int width, int height, SurfaceHolder DrawOn) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }
        mWidth = width;
        mHeight = height;
        mOutputHolder= DrawOn;         
        
        
        // Switch between native C and JAVA ()
        if(isNativeMethod){
        	Native_Setup();	        
        }else{
        	// Stable Java 
        	eglSetup();
	        makeCurrent();
	        setup();
        }
       
        
    }

    /**
     * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
     */
    private void setup() {
        mTextureRender = new Gl20TextureRender();
                
        mTextureRender.surfaceCreated();
                
        mTextureRender.setTextureID(mTextureId);        
        
        mTextureRender.finished_surfaceCreated();        

        if (VERBOSE) Log.d(TAG,"textureID=" + mTextureRender.getTextureId());
        mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());

        // This doesn't work if this object is created on the thread that CTS started for
        // these test cases.
        //
        // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
        // create a Handler that uses it.  The "frame available" message is delivered
        // there, but since we're not a Looper-based thread we'll never see it.  For
        // this to do anything useful, OutputSurface must be created on a thread without
        // a Looper, so that SurfaceTexture uses the main application Looper instead.
        //
        // Java language note: passing "this" out of a constructor is generally unwise,
        // but we should be able to get away with it here.
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);
        mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        mColorSwapBuf = new int[mWidth * mHeight];
                
    }

    /**
     * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
     */
    private void eglSetup() {
      	
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                attrib_list, 0);
        checkEglError("eglCreateContext");
        if (mEGLContext == null) {
            throw new RuntimeException("null context");
        }

        // Create a pbuffer surface.
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, mWidth,
                EGL14.EGL_HEIGHT, mHeight,
                EGL14.EGL_NONE
        };
               
        //bmp = Bitmap.createBitmap( mWidth, mHeight, Bitmap.Config.ARGB_8888);
        
        //pixels=Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888); 
        mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
        //mEGLSurface=NativeCreateSurface(mEGLDisplay);
        
        
        //mEGLSurface=EGL14.eglCreatePbufferFromClientBuffer(mEGLDisplay, , , configs, surfaceAttribs, 0);
        
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);        
        mTextureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        
        
        checkEglError("eglCreatePbufferSurface");
        if (mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }        
    }

    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;

        mSurface.release();

        // this causes a bunch of warnings that appear harmless but might confuse someone:
        //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
        //mSurfaceTexture.release();

        mTextureRender = null;
        mSurface = null;
        mSurfaceTexture = null;
      //  NaitveSourceRelease();
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
        
    }

    /**
     * Returns the Surface.
     */
    public Surface getSurface() {
        return mSurface;
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the OutputSurface object.  (More specifically, it must be called on the thread
     * with the EGLContext that contains the GL texture object used by SurfaceTexture.)
     */
    public void awaitNewImage() {
        final int TIMEOUT_MS = 2500;

        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            mFrameAvailable = false;
        }

        // Latch the data.
        mTextureRender.checkGlError("before updateTexImage");
        mSurfaceTexture.updateTexImage();
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     *
     * @param invert if set, render the image with Y inverted (0,0 in top left)
     */
    public void drawImage(boolean invert) {
        mTextureRender.drawFrame(mSurfaceTexture, invert);
    }

    // SurfaceTexture callback
    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        if (VERBOSE) Log.d(TAG, "new frame available");
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
    }

    /**
     * Saves the current frame to disk as a PNG image.
     */
    public void jDrawFrame() throws IOException {
    	
    	Log.v(TAG,"On Display");
    	long st=System.currentTimeMillis();  	
    	
    	
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            mPixelBuf);
        mPixelBuf.rewind();
        
        if(VERBOSE )Log.d(TAG,"Take "+String.valueOf(System.currentTimeMillis()-st)+"msec to perform Glreadpixel");

        int pixelCount = mWidth * mHeight;
        int[] colors = mColorSwapBuf;
        
        
        st=System.currentTimeMillis();
        mPixelBuf.asIntBuffer().get(colors);
        for (int i = 0; i < pixelCount; i++) {
            int c = colors[i];
            colors[i] = (c & 0xff00ff00) | ((c & 0x00ff0000) >> 16) | ((c & 0x000000ff) << 16);
        }
         Bitmap bmp = Bitmap.createBitmap(colors, mWidth, mHeight, Bitmap.Config.ARGB_8888);
         
         if(VERBOSE )Log.d(TAG,"Take "+String.valueOf(System.currentTimeMillis()-st)+"msec to perform tranform");
                          
         st=System.currentTimeMillis();
         Canvas c = mOutputHolder.lockCanvas();  
         Paint paint = new Paint();
         
         Rect dest = new Rect(0, 0,  mWidth,mHeight);
         c.drawColor(Color.GRAY);
         c.drawBitmap(bmp ,null,dest , paint);
         
         if (c != null) {
        	 mOutputHolder.unlockCanvasAndPost(c);
             //mSurfaceHolder.updateTexImage();
         }
         
         if(VERBOSE )Log.d(TAG,"Take "+String.valueOf(System.currentTimeMillis()-st)+"msec to draw on Surface");
         
    	st=System.currentTimeMillis();
        
        if(VERBOSE )Log.d(TAG,"Take "+String.valueOf(System.currentTimeMillis()-st)+"msec to finished the native glreadPixel");
    	
        
    }
    
    // Determine if we use the java draw frame or not.
    public void drawFrame()
    {
    	if(isNativeMethod){
    		NativeDrawFrame();    		
    	}
    	else{
    		try {
				jDrawFrame();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }    
    
    
    // Natvie Methods Java call Parts
     
    //Call the native function here
    public void Native_Setup()
    {
    	eglSetup();
        makeCurrent();
        setup();
        
        //do nothing now
        NativeSetup();
        setSurface(mOutputHolder.getSurface());
    }
  
        
    
    
    

    /**
     * Checks for EGL errors.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
    /** Native methods, implemented in jni folder */   
        
    public static native void NativeSetup();    
    public static native void NaitveSourceInit();
    public static native void NaitveSourceRelease();
    public static native void setSurface(Surface surface);
    public static native int  NaitveGetTexture(int width,int height);
    public static native void NativeDrawFrame();
    public static native void setFixedScale(int width,int height);
   
           
    
    /** Load jni .so on initialization */
   
    
    static {
    	System.loadLibrary("ui");                
        System.loadLibrary("utils");
        System.loadLibrary("cutils");
        System.loadLibrary("gui");
    	System.loadLibrary("nativegl");
    }
    
}



	

