package nsysu.ee.mmlab.playergl;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

public class Fullscreen_display extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_display);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fullscreen_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		String file_Path;
		TextView filepath_content;
		Button button1;				
		SurfaceView mSurfaceView;
		SurfaceHolder mHolder;
		
		
		boolean isInitializationComplete=false;
		boolean isDisplayComplete=true;
		MediaDecoder mDecoder;
		OutputSurface mOutputSurface;
		int mHeight,mWidth;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_fullscreen_display, container, false);
			return rootView;
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);  
			
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			//life cycle : called after the main activity onCreate().
			// get and bind listner here
			
			file_Path="/storage/emulated/0/test_stream/1.mp4";		
			
			
			filepath_content=(TextView)getActivity().findViewById(R.id.textView2);
			mSurfaceView=(SurfaceView)getActivity().findViewById(R.id.surfaceView2);
			
			mSurfaceView.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0) {
					
					//set to true first
					isInitializationComplete=true;
					if(isInitializationComplete==true && isDisplayComplete==true)
					{
						isDisplayComplete=false;
						doDecode();
					}
				}
				
			});
			
			
			
			
			mHolder= mSurfaceView.getHolder();
			mHolder.addCallback(new SurfaceHolder.Callback()
			{

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {				
					mHeight= height;
					mWidth= width;				
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
			}); t.start();
			
			Log.v("Panel Activity","OutputSurface Created" );				
			
			
		}
		
	}

}
