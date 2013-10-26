package com.ffmpegtest.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoFileDBHelper extends SQLiteOpenHelper {
	public VideoFileDBHelper(Context context) {
		super(context, "videoFileDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE videoFileDB (filePath text, fileName text);";
		db.execSQL(sql);
		sql = "CREATE TABLE playTimeDB (file text, playTime integer);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + "videoFileDB;";
		db.execSQL(sql);
	}
}
