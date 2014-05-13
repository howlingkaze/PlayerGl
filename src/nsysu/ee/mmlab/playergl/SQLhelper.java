package nsysu.ee.mmlab.playergl;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLhelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME="sec_db";
    private final static int DATABASE_VERSION=1;
    private final static String TABLE_NAME="sec_pwd";
    public final static String FIELD_ID="_id"; 
    public final static String FIELD_TITLE="sec_Title";
	
	public SQLhelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public long insert(String Title)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues(); 
        cv.put(FIELD_TITLE, Title);
        long row=db.insert(TABLE_NAME, null, cv);
        return row;
    }
    
    public void delete(int id)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        String where=FIELD_ID+"=?";
        String[] whereValue={Integer.toString(id)};
        db.delete(TABLE_NAME, where, whereValue);
    }
    
    public void update(int id,String Title)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        String where=FIELD_ID+"=?";
        String[] whereValue={Integer.toString(id)};
        ContentValues cv=new ContentValues(); 
        cv.put(FIELD_TITLE, Title);
        db.update(TABLE_NAME, cv, where, whereValue);
    }

}
