package nsysu.ee.mmlab.playergl;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;
import android.graphics.Canvas;
import android.provider.Settings.System; 

public class BacklitVsColorTest extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backlit_vs_color_test);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.backlit_vs_color_test, menu);
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

		SurfaceView mSurfaceView;
		SurfaceHolder mSurfaceHolder;
		int SwitchLevel = 5;
		long downTime=0;
    	float init_x,init_y;
    	float pre_x,pre_y;
    	int src_x,src_y; // Touch pannel size    	
		final float touch_region_threshold=0.05f;
		boolean Scale_region_touched=false;
		
		
		int backlight_sacle[] = {25,35,45,55,65,75};
		TextView scaleTextView;
		
		
		int bl_scale= 0;
		
		int PrimayColor[] = {Color.rgb(255,0,0),Color.rgb(0,255,0),Color.rgb(0,0,255)};		
		int colorNumber=3;
		int colorChangeCount =0;
		float left_scale_var_size=0.2f;
		
	    Context mcontext;
	    ContentResolver cResolver;
		
		int oneChannelColorScale[] = {25,35,45,55,65,75};
		
		
		final float clickDisThreshold = 0.02f;
		final long clickTimeThreshold = 2000;
		
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){		
			View rootView = inflater.inflate(R.layout.fragment_backlit_vs_color_test, container, false);

			rootView.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// We only handle single point touch now;
					
			      	final float px= event.getX();
			      	final float py = event.getY();	
					
		           	switch (event.getAction())
		           	{
			           	case MotionEvent.ACTION_DOWN: {		                	
			               	init_x = px; pre_x=px;
			               	init_y = py; pre_y=py;		                	
			               	Log.v("Rootview touch down","x: "+Float.toString(init_x)+" y: "+Float.toString(init_y)+" t :"+String.valueOf(downTime));
			               	
			               	
			               	// determine the touch region			               	
			               	if ( init_x < src_x*left_scale_var_size)
			               	{
			               		Scale_region_touched=true;
			               		Log.v("Left scale bar region","");				            		
			               		
			               	}
			               	
			               	
		             	    break;    	
		                }
			           	case MotionEvent.ACTION_MOVE : {
			           		
			           		
			           		Log.v("Rootview Move","x: "+Float.toString(px)+" y: "+Float.toString(py));
			           		if( init_x > src_x*left_scale_var_size)
			           			Scale_region_touched =false;      		 		
			           		
			           		if(Scale_region_touched == true)
			           		{
			           			
			           			bl_scale += ((pre_y-py)/5);
			           			if(bl_scale > 255) bl_scale=255;
			           			if(bl_scale < 0)bl_scale=0;
			           			scaleTextView.setText(Float.toString(bl_scale));			           			
			           			//Log.v("In move scale",Float.toString(bl_scale));
			           			pre_x=px;
				               	pre_y=py;
			           		}
			            	break;
			           	}		           	
			           	case MotionEvent.ACTION_UP :{
			           					           		
			           		if(Scale_region_touched == true)
			           			break;
		                	float diffx,diffy;
		                	diffx= event.getX()-init_x;
		                	diffy= event.getY()-init_y;
		                	Log.v("Rootview touch up","");
		                	if( Math.abs(diffx)<10 && Math.abs(diffy)<10 )
		                	{          		
		                		
		                		Log.v("Touch Event","Click detect");
		                		
		                	}else
		                	{
		                		
		                		if(Math.abs(diffx) > Math.abs(diffy))
		                		{
		                			// vertical 
		                			if( diffx > 0)
		                			{		                				
		                				bl_scale++;
		                				bl_scale=(bl_scale+5)%5;
		                				BlChange(backlight_sacle[bl_scale]);
		                				Log.v("Touch Event","slide right");
		                			}else
		                			{		                			
		                				bl_scale--;
		                				bl_scale=(bl_scale+5)%5;
		                				BlChange(backlight_sacle[bl_scale]);
		                				Log.v("Touch Event","slide left");
		                			}
		                			
		                		}else
		                		{
		                			//horizontal
		                			if( diffy > 0)
		                			{		                				
		                				Log.v("Touch Event","slide down");
		                				colorChangeCount--;
		                			}else
		                			{		                				
		                				Log.v("Touch Event","slide top");
		                				colorChangeCount++;
		                			}
		                			if (colorChangeCount< 0) colorChangeCount+=colorNumber;
		                			drawOnViewSingleColor(PrimayColor[(colorChangeCount)%colorNumber]);		                			
		                		}
		                		
		                		Log.v("Touch Event"," Slide");
		                	}		                	
		                }
			           	Scale_region_touched= false;
		            }
		           	return true;
				}
			});
			return rootView;
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);  	
			
			scaleTextView = (TextView)getActivity().findViewById(R.id.textViewBlScale);	
			
			mSurfaceView = (SurfaceView)getActivity().findViewById(R.id.surfaceViewColorTest);	
			mSurfaceHolder = mSurfaceView.getHolder();
			mSurfaceHolder.addCallback(new SurfaceHolder.Callback()
			{

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {	
					
					src_x = width;
					src_y = height;
		           	Log.v("Surface Create","width: "+Integer.toString(src_x)+" height: "+Integer.toString(src_y));
					
				}
				@Override
				public void surfaceCreated(SurfaceHolder holder) {					
				}
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {					
				}
				
			});
			
	
			
			mcontext = getActivity().getApplicationContext();
			cResolver=mcontext.getContentResolver();
		}		
		
		public void drawOnViewSingleColor(int color) //
		{
				
			Canvas canvas = mSurfaceHolder.lockCanvas();
			canvas.drawColor(color);			
			mSurfaceHolder.unlockCanvasAndPost(canvas);
		}
		public void BlChange(int bl)
		{			
			System.putInt(cResolver, System.SCREEN_BRIGHTNESS, bl );
		}

	}

}
