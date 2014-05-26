package nsysu.ee.mmlab.playergl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLhelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME="display_setting.db";
	
    private final static int DATABASE_VERSION=1;    
    
    private final static String TABLE_NAME="DisplaySetting"; // db Name??
   
    //Associated SQLite instructions are in jin/SQLite.txt which have been tested.
	
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
				"attr INTEGER"+			
				");";
		
		db.execSQL(SQL);			
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	
	public void insert(int groupid,int childid)
    {
		String SQL = "INSERT INTO ?(id,groupid,attr) VALUES (NULL,?,?);";
		SQLiteDatabase db=this.getWritableDatabase();
		db.execSQL(SQL,new String[]{TABLE_NAME,Integer.toBinaryString(groupid),Integer.toBinaryString(childid)});

    }
	int getAttr(int groupid)
	{
		String SQL = "SELECT * FROM ? WHERE groupid=?;";
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cr = db.rawQuery(SQL,new String[]{TABLE_NAME,Integer.toBinaryString(groupid)});
		cr.moveToFirst();
		int attr=cr.getInt(2); // id:0 groups:1 attr:2
		return attr;
	}
    
    public void delete(int id)
    {
    	/*
        SQLiteDatabase db=this.getWritableDatabase();
        String where=FIELD_ID+"=?";
        String[] whereValue={Integer.toString(id)};
        db.delete(TABLE_NAME, where, whereValue);
        */
    }
    
    public void update(int updateTarget,int value)
    {
    	String SQL = "UPDATE ? SET attr=? WHERE groupid=? ;";
    	SQLiteDatabase db=this.getWritableDatabase();
		db.execSQL(SQL,new String[]{TABLE_NAME,Integer.toBinaryString(value),Integer.toBinaryString(updateTarget)});
    }
    public void dropTable(String dbname)
    {
    	String SQL ="DROP TABLE IF EXIST setting";
    	SQLiteDatabase db=this.getWritableDatabase();
		db.execSQL(SQL,null);
    }
    
  

}
