package nsysu.ee.mmlab.playergl;
 
import java.util.List;
import java.util.Map;
 
import nsysu.ee.mmlab.playergl.R;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Activity context;
    private Map<String, List<String>> SettingMenu;
    private List<String> Menu;
    private TextView selectedView = null;
 
    public ExpandableListAdapter(Activity context, List<String> Menu,
            Map<String, List<String>> SettingMenu) {
        this.context = context;
        this.SettingMenu = SettingMenu;
        this.Menu = Menu;
    }
 
    public Object getChild(int groupPosition, int childPosition) {
        return SettingMenu.get(Menu.get(groupPosition)).get(childPosition);
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
    	Log.v("getChildView","Called");
        final String selection = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();
 
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.setting_layer2, null);
        }
 
        TextView item = (TextView) convertView.findViewById(R.id.Selection);
        
        
        item.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.v("getChildView","child clicked");
				if(selectedView != null)
				{
					selectedView.setText(selection);
				}
				
			}
        	
        	
        	
        });
        
 
        
        
        //ImageView delete = (ImageView) convertView.findViewById(R.id.delete);
        /*
        delete.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to remove?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                List<String> child =
                                    SettingMenu.get(Menu.get(groupPosition));
                                child.remove(childPosition);
                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });*/
        
        
        
        item.setText(selection);
        return convertView;
    }
 
    public int getChildrenCount(int groupPosition) {
        return SettingMenu.get(Menu.get(groupPosition)).size();
    }
 
    public Object getGroup(int groupPosition) {
        return Menu.get(groupPosition);
    }
 
    public int getGroupCount() {
        return Menu.size();
    }
 
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent)
    {    	
        String selectionName = (String) getGroup(groupPosition);
        if (convertView == null)
        {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.setting_layer1,null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.Selection);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(selectionName);
        if(isExpanded)
        {
        	selectedView = (TextView)convertView.findViewById(R.id.Selected);
        }
        else
        {
        	selectedView = null;
        }
        
        Log.v("getGroupView","Called");
        
        
        return convertView;
    }
 
    public boolean hasStableIds() {
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}