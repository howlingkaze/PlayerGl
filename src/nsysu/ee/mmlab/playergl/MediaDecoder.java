package nsysu.ee.mmlab.playergl;

import java.io.IOException;
import java.nio.ByteBuffer;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

public class MediaDecoder {

	final static String TAG = "MediaDecoder";	
	private static final boolean VERBOSE = true;
	// true to trigger log output.
	
	private MediaExtractor mMediaExtractor = null;
	private MediaCodec mMediaCodec = null;
	private Surface outputSurface = null;
	private String mPath = null;	
	final static String VIDEO_MIME_PREFIX = "video/"; 
	private int mVideoTrackIndex = -1;
	private OutputSurface mOutputSurface;
	
	
	public MediaDecoder(String path ,OutputSurface out ,int width, int height ,SurfaceHolder mHolder)
	{		
		mPath = path;			
		//mOutputSurface = out;
		initOutputSurface(width,height,mHolder);
		initCodec();
		
	}
	
	private boolean initCodec()
	{
		Log.i(TAG, "initCodec");
		
		// Extract VideoTrack from file
		mMediaExtractor = new MediaExtractor();		
		
		try {
			mMediaExtractor.setDataSource(mPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		int trackCount = mMediaExtractor.getTrackCount();
		for (int i = 0; i < trackCount; ++i) {
			MediaFormat mf = mMediaExtractor.getTrackFormat(i);
			String mime = mf.getString(MediaFormat.KEY_MIME);
			if (mime.startsWith(VIDEO_MIME_PREFIX)) {
				mVideoTrackIndex = i;
				break;
			}
		}
		if (mVideoTrackIndex < 0) 
			return false;
		
		//Get MetaData from file		
		mMediaExtractor.selectTrack(mVideoTrackIndex);
		MediaFormat mf = mMediaExtractor.getTrackFormat(mVideoTrackIndex);
		String mime = mf.getString(MediaFormat.KEY_MIME);
				
		//Initial MediaCodec
		mMediaCodec = MediaCodec.createDecoderByType(mime);		
		mMediaCodec.configure(mf, outputSurface, null, 0);
		mMediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
		mMediaCodec.start();
		
		
		if (VERBOSE) Log.i(TAG, "initCodec end");
		
		return true;
	}
	private boolean initOutputSurface(int width, int height ,SurfaceHolder mHolder)
	{
		mOutputSurface= new OutputSurface(width,height, mHolder);
		
		outputSurface = mOutputSurface.getSurface();	
		
		return true;
	}
	
	
	
	public void doDecode()
	{
		DecodeFrame();/*
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {				
					DecodeFrame();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}); t.start();*/
	}
	public void ReleaseSource()
	{
		if (outputSurface != null) {
            outputSurface.release();
            outputSurface = null;
        }
        if (mMediaCodec != null) {
        	mMediaCodec.stop();
        	mMediaCodec.release();
        	mMediaCodec = null;
        }
        if (mMediaExtractor != null) {
        	mMediaExtractor.release();
        	mMediaExtractor = null;
        }
	}
	public void DecodeFrame()
	{
		final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = mMediaCodec.getInputBuffers();
        
        ByteBuffer[] decoderOutputBuffers = mMediaCodec.getOutputBuffers();
        
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        long frameSaveTime = 0;
        long renderTime = 16;

        boolean outputDone = false;
        boolean inputDone = false;
        Long startMs=System.currentTimeMillis();
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                	
                	
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = mMediaExtractor.readSampleData(inputBuf, 0);
                    
                    if (chunkSize < 0)
                    {
                        // End of stream -- send empty frame with EOS flag set.
                    	mMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    }
                    else
                    {
                        if (mMediaExtractor.getSampleTrackIndex() != mVideoTrackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                            		mMediaExtractor.getSampleTrackIndex() + ", expected " + mVideoTrackIndex);
                        }
                        
                        long presentationTimeUs = mMediaExtractor.getSampleTime();
                        mMediaCodec.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        mMediaExtractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_USEC);
                
                
                
                
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
                {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                }
                else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                }
                else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                {
                    MediaFormat newFormat = mMediaCodec.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                }
                else if (decoderStatus < 0)
                {
                    Log.e(TAG,"unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                }
                else
                { // decoderStatus >= 0
										
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                    {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }
                   
                    boolean doRender=true;
                    
                    //if (VERBOSE) Log.d(TAG, "Decoder Status " + info.size);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
   		    		long timenow=System.currentTimeMillis();    				    				    		
    		    			    		
    		    		//InterfaceControl.mGLView.getCurrentFrame();
    		    		//Try to get framedata		    		
    						
   		    		while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs - renderTime) {
    		    	        try {
    		    	          Thread.sleep(10);
    		    	        } catch (InterruptedException e) {
    		    	          e.printStackTrace();
    		    	          break;
    		    	        }
    		    	}    	
    		    	
                    mMediaCodec.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender)
                    {
                        if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        
                        mOutputSurface.awaitNewImage();								
                        mOutputSurface.drawImage(true);
                        long t=System.currentTimeMillis();
                        mOutputSurface.drawFrame();
						long e=System.currentTimeMillis();
						Log.v(TAG,"Take "+String.valueOf(e-t)+"msec to perform transform and draw");
                        
                
                    }
                }
            }
        }

			
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
