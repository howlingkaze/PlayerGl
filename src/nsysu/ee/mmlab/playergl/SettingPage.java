package nsysu.ee.mmlab.playergl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import nsysu.ee.mmlab.playergl.SettingPage;

public class SettingPage extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_page);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	
	@Override  
    protected void onStart() {  
        super.onStart();  
        ActionBar actionBar = this.getActionBar();  
        actionBar.setDisplayHomeAsUpEnabled(true);  
    }  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting_page, menu);
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

		List<String> groupList; 
	    List<String> childList;
	    Map<String, List<String>> SettingMenu;
	    ExpandableListView expListView;
	    TextView SelectedGroupText;	    
	    SQLhelper  myDBhelper;
	    
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_setting_page,container, false);
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			createGroupList();
			 
	        createSubSelection();
	 
	        expListView = (ExpandableListView) getActivity().findViewById(R.id.expandableListView1);
	        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(getActivity(), groupList,SettingMenu);
	        
	        expListView.setAdapter(expListAdapter);
	 
	        //setGroupIndicatorToRight();
	 
	        expListView.setOnChildClickListener(new OnChildClickListener() {
	 
	            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id)
	            {
	                final String selected = (String) expListAdapter.getChild(groupPosition, childPosition);
	                
	                String group = (String) expListAdapter.getGroup(groupPosition);
	                //TextView temp= (TextView) v.findViewById(R.id.Selected);
	                //temp.setText(selected);
              	    Log.v("in childClickListner","click!");
	                //Toast.makeText(getActivity().getBaseContext(), selected, Toast.LENGTH_LONG).show();	 
              	    parent.collapseGroup(groupPosition);
              	  SelectedGroupText.setText(selected);
	                return true;
	            }
	        });
	        /*
	        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {  
	            @Override  
	            public void onGroupExpand(int groupPosition) {  
	                
	            }  
	        });  */
	        expListView.setOnGroupClickListener(new OnGroupClickListener() {  
	            @Override  
	            public boolean onGroupClick(ExpandableListView parent, View clickedView, int groupPosition, long groupId) {  
	                 
	            	SelectedGroupText= (TextView)clickedView.findViewById(R.id.Selected);
	            	if(SelectedGroupText==null)
	            		Log.v("OnGroupClickListener","Can't get current Expanded textView");
	            	            	
	                return false;  
	            }  
	        });
	        
	        dbInitialized();
			
		}	
		// This parts is for database access
		private void dbInitialized()
		{
			myDBhelper.getWritableDatabase();
		}
		
		
		//
		private void createGroupList() {
	        groupList = new ArrayList<String>();
	        groupList.add("Test Method");
	        groupList.add("Residual");
	        groupList.add("Enable Component");

	    }
	 
	    private void createSubSelection() {
	    		    	

	        String[] testMethods = { "Sobel", "something else"};        
	        //Display size, which affect the speed of transmission.
	        String[] testResidual = { "1280x720", "1920x1080" };
	        String[] enableSetting = {"fortest1","fortest2","fortest3"};
 
	        SettingMenu = new LinkedHashMap<String, List<String>>();
	 
	        // save the setting into group list 
	        for (String tempString : groupList)
	        {
	            if ( tempString.equals("Test Method") )
	            {
	                loadChild(testMethods);
	            }else if ( tempString.equals("Residual") ){
	            	loadChild(testResidual);	            	
	            } else if ( tempString.equals("Enable Component")){
	            	loadChild(enableSetting);
	            }        	
	            	
	            SettingMenu.put(tempString, childList);
	        }
	    }
	 
	    private void loadChild(String[] ChildSelection) {
	        childList = new ArrayList<String>();
	        for (String model : ChildSelection)
	            childList.add(model);
	    }
	 

	    private void setGroupIndicatorToRight()
	    {
	        /* Get the screen width */
	        DisplayMetrics dm = new DisplayMetrics();
	        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
	        int width = dm.widthPixels;
	 
	        expListView.setIndicatorBounds(width - getDipsFromPixel(35), width- getDipsFromPixel(5));
	    }
	 
	    // Convert pixel to dip
	    public int getDipsFromPixel(float pixels)
	    {
	        // Get the screen's density scale
	        final float scale = getResources().getDisplayMetrics().density;
	        // Convert the dps to pixels, based on density scale
	        return (int) (pixels * scale + 0.5f);
	    }
	 
		
		
	}

}
