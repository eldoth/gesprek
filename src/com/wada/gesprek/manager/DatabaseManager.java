package com.wada.gesprek.manager;

import com.wada.gesprek.R;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "Gesprek_db";
	
	private static final Integer DATABASE_VERSION = 1;
	
	private final Context contexto;


	public DatabaseManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.contexto = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] sql = contexto.getString(R.string.DatabaseManager_onCreate).split("\n");
		db.beginTransaction();
		try {
			execMultipleSQL(db, sql);
			db.setTransactionSuccessful();
		} catch (SQLiteException e){
			Log.e("Erro ao criar o banco de dados", e.toString());
			throw e;
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	private void execMultipleSQL(SQLiteDatabase db, String[] sql) {
		for (String s : sql) {
			if (s.trim().length() > 0) {
				db.execSQL(s);
			}
		}
	}

}
