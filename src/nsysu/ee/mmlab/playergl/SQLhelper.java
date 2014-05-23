package nsysu.ee.mmlab.playergl;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLhelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME="display_setting.db";
	
    private final static int DATABASE_VERSION=1;    
    
    private final static String TABLE_NAME="Setting"; // db Name??
    
    
    /** 
     * CREATE TABLE IF NOT EXISTS display_config .setting(
     * id INTEGER PRIMARY KEY ASC,
	   groupid INTEGER,
	   group_name TEXT,
	   attr INTEGER,
	   attr_name TEXT,							
		);
		
		//form structure
	
     */
    
	
	public SQLhelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
		//create empty form		
		final String SQL = 
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
				"( "+
				"id INTEGER PRIMARY KEY ASC,"+
				"groupid INTEGER,"+
				"group_name TEXT,"+
				"attr INTEGER,"+
				" attr_name TEXT,"+
				");";
		
		db.execSQL(SQL);				
	  
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	
	public void insert(String Title)
    {
		/*
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues(); 
        cv.put(FIELD_TITLE, Title);
        long row=db.insert(TABLE_NAME, null, cv);
        return row;*/
    }
    
    public void delete(int id)
    {
    	/*
        SQLiteDatabase db=this.getWritableDatabase();
        String where=FIELD_ID+"=?";
        String[] whereValue={Integer.toString(id)};
        db.delete(TABLE_NAME, where, whereValue);*/
    }
    
    public void update(int id,String Title)
    {
    	/*
        SQLiteDatabase db=this.getWritableDatabase();
        String where=FIELD_ID+"=?";
        String[] whereValue={Integer.toString(id)};
        ContentValues cv=new ContentValues(); 
        cv.put(FIELD_TITLE, Title);
        db.update(TABLE_NAME, cv, where, whereValue);
        */
    }
    public void dropTable(String dbname)
    {
    	
    }
    
    public void buildDefaultTable()
    {
    	
    }    

}
