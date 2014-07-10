package com.alexsum.tcw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
	      super(context, "baseTCW", null, 1);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	      Log.d("DBHelper", "--- onCreate database ---");
	      db.execSQL("create table stat (id integer primary key autoincrement,date text,incom integer, outgo integer, prc integer);");
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	    }
}
