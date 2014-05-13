package nsysu.ee.mmlab.playergl;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

public class UserInterface extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_interface);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new Fragment_setting()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_interface, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			Intent intent = new Intent();
	        intent.setClass(UserInterface.this, SettingPage.class);		        ;
	        startActivity(intent);			
			//return true;
		}		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class Fragment_setting extends Fragment {
		
		
		String file_Path;
		TextView filepath_content;
		Button button1,button2;				
		SurfaceView mSurfaceView;
		SurfaceHolder mHolder;
		
		MediaDecoder mDecoder;
		int mHeight,mWidth;
		
		public Fragment_setting() {			
			// can't initiate anything, dunno why/
		}		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user_interface,
					container, false);
			return rootView;
		}
		
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);  
			//life cycle : called after the main activity onCreate().
			// get and bind listner here
			
			file_Path="/storage/emulated/0/test_stream/1.mp4";		
			button1=(Button)getActivity().findViewById(R.id.button1);
			button1.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View arg0)
				{										
					doDecode();					
				} 
			});
			
			button2=(Button)getActivity().findViewById(R.id.button2);
			button2.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
			        intent.setClass(getActivity(), Fullscreen_display.class);		        ;
			        startActivity(intent);			
					
				}
				
			});
			
			
			filepath_content=(TextView)getActivity().findViewById(R.id.textView2);
			mSurfaceView=(SurfaceView)getActivity().findViewById(R.id.surfaceView1);
			mHolder= mSurfaceView.getHolder();
			mHolder.addCallback(new SurfaceHolder.Callback()
			{

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {
					
					mHeight= height;
					mWidth= width;				
					Log.v("SurfaceView in User interface : ",String.valueOf(mHeight)+" "+
					String.valueOf(mWidth));
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					// TODO Auto-generated method stub					
				}
				
			});
			
			
			
		}	
		
		private void doDecode()
		{	
			//mHeight = mTextureView.getHeight();
			//mWidth = mTextureView.getWidth();			
					
			//mDecoder= new MediaDecoder(file_Path,mOutputSurface,mWidth,mHeight,mHolder);
			//mDecoder.doDecode();	
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						
                        mDecoder= new MediaDecoder(file_Path,mWidth,mHeight,mHolder);												
						mDecoder.doDecode();	
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}			
			});
			t.start();
			
			Log.v("Panel Activity","OutputSurface Created" );			
			
			
		}
		
		
	}

}
