package com.ffmpegtest.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.ffmpegtest.helpers.VideoFileDBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class VideoFileDBAdapter {
	static final String video_file_db_name = "videoFileDB";
	static final String play_time_db_name = "playTimeDB";

	private Context context;
	private SQLiteDatabase db;

	public VideoFileDBAdapter(Context context) {
		this.context = context;
		this.open();
	}

	public void open() throws SQLException {
		try {
			db = (new VideoFileDBHelper(context).getWritableDatabase());
		} catch(SQLiteException e) {
			db = (new VideoFileDBHelper(context).getReadableDatabase());
		}
	}

	public void saveVideoFileDB(ArrayList<File> videoList) {
		for(int i=0; i<videoList.size(); i++) {
			try {
				ContentValues values = new ContentValues();
				values.put("filePath", videoList.get(i).getParent());
				values.put("fileName", videoList.get(i).getName());
				db.insert(video_file_db_name, null, values);
			} catch(Exception e) {}
		}
	}

	public void saveVideoTime(String AbsoulteFilePath, int playTime, String finger) {
		if(playTime == 0)
			playTime = 1;
		
		ContentValues cv = new ContentValues();
		cv.put("file", AbsoulteFilePath);
		cv.put("playTime", playTime);
		cv.put("finger", finger);

		if(getVideoTime(AbsoulteFilePath) == 0) {
			db.insert(play_time_db_name, null, cv);
		} else {
			String whereClause = "file = ?";
			String[] whereArgs = new String[] { AbsoulteFilePath };
			db.update(play_time_db_name, cv, whereClause, whereArgs);
		}
		
	}
	
	public String getVideoFingerPrint(String AbsoulteFilePath) {
		Cursor c = selectPlayTimeDB();
		String finger = "";
		if(c.moveToFirst()) {
			int idxFile = c.getColumnIndex("file");
			int idxFinger = c.getColumnIndex("finger");

			do {
				String file = c.getString(idxFile);
				String tmp = c.getString(idxFinger);
				Log.e("DB","file = "+file);

				if(AbsoulteFilePath.equals(file)) {
					finger = tmp;
					break;
				}
				
			} while(c.moveToNext());
		}
		return finger;
	}

	private Cursor selectVideoFileDB() {
		Cursor c = db.query(video_file_db_name, new String[] {"filePath", "fileName"}, null, null, null, null, null);

		return c;
	}

	private Cursor selectPlayTimeDB() {
		Cursor c = db.query(play_time_db_name, new String[] {"file", "playTime", "finger"}, null, null, null, null, null);

		return c;
	}

	//	public void updateVideoFileDB(int newVideo, String path, String name, int playTime) {
	//		ContentValues cv=new ContentValues(); 
	//		cv.put("newVideo", newVideo);
	//		cv.put("playTime",playTime);
	//		cv.put("fileName", name);
	//
	//		String whereClause = "filePath="+  "\'" +path+"\'"+" and fileName="+"\'"+name+"\'"; 
	//		String[] whereArgs = new String [] { path, name };
	//		Log.e("newVideo = "+newVideo, "path = " + path);
	//		Log.e("name = "+name, "time = " + playTime);
	//
	//		db.update( video_file_db_name, cv, whereClause , null);
	//	}

	public void removeVideoFileDB() {
		db.delete(video_file_db_name, null, null);
	}

	public HashMap<String, ArrayList<File>> getVideoFileDB() {
		HashMap<String, ArrayList<File>> video = new HashMap<String, ArrayList<File>>();
		Cursor c = selectVideoFileDB();
		if(c.moveToFirst()) {
			int idxFilePath = c.getColumnIndex("filePath");
			int idxFileName = c.getColumnIndex("fileName");

			do {
				String filePath = c.getString(idxFilePath);
				String fileName = c.getString(idxFileName);
				if(!video.containsKey(filePath))
					video.put(filePath, new ArrayList<File>());

				video.get(filePath).add(new File(filePath + '/' + fileName));
				Log.e("getVideoFileDB", fileName);
			} while(c.moveToNext());
		}

		return video;
	}

	public int getVideoTime(String video) {
		Cursor c = selectPlayTimeDB();
		int time = 0;
		if(c.moveToFirst()) {
			int idxFile = c.getColumnIndex("file");
			int idxPlayTime = c.getColumnIndex("playTime");

			do {
				String file = c.getString(idxFile);
				Log.e("fileName = " + file, "time = " + c.getInt(idxPlayTime));

				if(video.equals(file)) {
					time = c.getInt(idxPlayTime);
					break;
				}
				
			} while(c.moveToNext());
		}

		return time;
	}
}
