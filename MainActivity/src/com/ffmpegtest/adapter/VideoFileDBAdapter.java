package com.ffmpegtest.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.ffmpegtest.VideoFile;
import com.ffmpegtest.helpers.VideoFileDBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class VideoFileDBAdapter {
	static final String db_name = "videoFileDB";
	
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
	
	public void saveVideoFileDB(ArrayList<VideoFile> videoList) {
		for(int i=0; i<videoList.size(); i++) {
			try {
				ContentValues values = new ContentValues();
				values.put("newVideo", 0);
				values.put("filePath", videoList.get(i).getPath());
				values.put("fileName", videoList.get(i).getName());
				values.put("playTime", videoList.get(i).getTime());
				db.insert(db_name, null, values);
			} catch(Exception e) {}
		}
	}

	private Cursor selectVideoFileDB() {
		Cursor c = db.query(db_name, new String[] {"newVideo", "filePath", "fileName", "playTime"}, null, null, null, null, null);
		
		return c;
	}
	
	public void updateVideoFileDB(int newVideo, String path, String name, int playTime) {
		ContentValues cv=new ContentValues(); 
		cv.put("newVideo", newVideo);
		cv.put("playTime",playTime);

		String whereClause = "filePath="+  "\'" +path+"\'"+" and fileName="+"\'"+name+"\'"; 
		String[] whereArgs = new String [] { path, name };
		Log.e("newVideo = "+newVideo, "path = " + path);
		Log.e("name = "+name, "time = " + playTime);

		db.update( db_name, cv, whereClause , null);
	}
	
	public void removeVideoFileDB() {
		db.delete(db_name, "newVideo = 0", null);
		db.delete(db_name, "newVideo = 1", null);
	}
	
	public HashMap<String, ArrayList<VideoFile>> getVideoFileDB() {
		HashMap<String, ArrayList<VideoFile>> video = new HashMap<String, ArrayList<VideoFile>>();
		Cursor c = selectVideoFileDB();
		if(c.moveToFirst()) {
			int idxNewVideo = c.getColumnIndex("newVideo");
			int idxFilePath = c.getColumnIndex("filePath");
			int idxFileName = c.getColumnIndex("fileName");
			int idxPlayTime = c.getColumnIndex("playTime");
			
			do {
				int newVideo = c.getInt(idxNewVideo);
				String filePath = c.getString(idxFilePath);
				String fileName = c.getString(idxFileName);
				int playTime = c.getInt(idxPlayTime);
				if(!video.containsKey(filePath))
					video.put(filePath, new ArrayList<VideoFile>());
				
				video.get(filePath).add(new VideoFile(filePath, fileName, playTime, newVideo));
				
			} while(c.moveToNext());
		}
		
		return video;
	}
}
