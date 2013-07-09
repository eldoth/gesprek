package com.wada.gesprek.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
		// TODO Auto-generated method stub
		String[] sql = 
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
