package com.ffmpegtest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ffmpegtest.adapter.VideoListAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

public class MainActivity extends Activity implements OnItemClickListener {

	private ActionBar mActionBar;
	private SearchView mSearchView;
	private HashMap<String, ArrayList<String>> video;
	private ArrayList<String> path;
	private ArrayList<String> name;
	private ListView listView;
	private String currentPath;
	private String root;
	public static final String supportedVideoFileFormats[] = 
		{   "mp4","wmv","avi","mkv","dv",
		"rm","mpg","mpeg","flv","divx",
		"swf","dat","h264","h263","h261",
		"3gp","3gpp","asf","mov","m4v", "ogv",
		"vob", "vstream", "ts", "webm",
		"vro", "tts", "tod", "rmvb", "rec", "ps", "ogx",
		"ogm", "nuv", "nsv", "mxf", "mts", "mpv2", "mpeg1", "mpeg2", "mpeg4",
		"mpe", "mp4v", "mp2v", "mp2", "m2ts",
		"m2t", "m2v", "m1v", "amv", "3gp2"
		};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		listView = (ListView)findViewById(R.id.list);
		listView.setOnItemClickListener(this);

		currentPath = Environment.getExternalStorageDirectory().getPath();
		root = Environment.getExternalStorageDirectory().getPath();
		path = new ArrayList<String>();
		name = new ArrayList<String>();
		video = new HashMap<String, ArrayList<String>>();

		getVideoFileList();
	}

	/** 
	 * 작성자 : 임창민
	 * 메소드 이름 : getVideoFileList()
	 * 매개변수 : 없음
	 * 반환값 : 없음
	 * 메소드 설명 : HashMap에 비디오 파일Path와 리스트를 초기화한다.
	 */ 
	public void getVideoFileList() {

//		String[] videoProjection = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.SIZE };
//		Cursor videoCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, null);
//
//		videoCursor.moveToFirst();
//
//		if(videoCursor != null) {
//			do {
//				int videoPath = videoCursor.getColumnIndex(MediaStore.Video.Media.DATA);
//
//				String[] videoFiles = videoCursor.getString(videoPath).split("/");
//				String videoFilePath = "";
//				for(int i=0; i<videoFiles.length - 1; i++) {
//					videoFilePath += videoFiles[i] + "/";
//				}
//
//				File f = new File(videoFilePath);
//				if(f.canRead()) {
//					if(!video.containsKey(f.getAbsolutePath())) {
//						video.put(f.getAbsolutePath(), new ArrayList<String>());
//						Log.e("newKey", f.getAbsolutePath());
//					}
//
//					String videoName = videoFiles[(videoFiles.length - 1)];
//					video.get(f.getAbsolutePath()).add(videoName);
//					Log.e("addValue : " + f.getAbsolutePath(), videoName);
//				}
//
//			} while(videoCursor.moveToNext());
//		}
		
		getDir(root);
//		path = new ArrayList<String>(video.keySet());
//		for(int i=0; i<path.size(); i++) {
//			String[] pathList = path.get(i).split("/");
//			name.add(pathList[pathList.length - 1]);
//		}
//		listView.setAdapter(new VideoListAdapter(this, video, currentPath)); 
	}
	
	public void getDir(String str_path) {
		File f = new File(str_path);
		File[] files = f.listFiles();
		for(int i=0; i<files.length; i++) {
			File file = files[i];
			if(file.isDirectory() && !file.isHidden())
				getDir(file.getAbsolutePath());
			else
				Log.e("file = ", file.getAbsolutePath());
				if(isVideoFile(file.getName()))
					Log.e(file.getPath(), file.getName());
		}
	}

	public static Boolean isVideoFile(String file){
		if(file==null || file ==""){
			return false;
		}
		String ext = file.toString();
		String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
		if (Arrays.asList(supportedVideoFileFormats).contains(sub_ext.toLowerCase())){
			return true; 
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);

		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle("폴더");

		mSearchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
		mSearchView.setQueryHint("비디오 파일 검색");
		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				ArrayList<String> key = new ArrayList<String>(video.keySet());

				for(int i=0; i<key.size(); i++) {
					ArrayList<String> value = video.get(key.get(i));
					for(int j=0; j<value.size(); j++) {
						String file = key.get(i) + '/' + value.get(j);
						if(file.contains(query))
							Log.e("Search File", file);
					}
				}

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				Log.e("체인지", newText);
				return false;
			}
		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_refresh:
			break;
		}

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id)
	{
		File f = null;
		if(currentPath.equals(root)) {
			f = new File(path.get(position));
			currentPath = f.getAbsolutePath();
		}

		else {
			Log.e("currentPath : ", currentPath);
			Log.e("name : ", video.get(currentPath).get(position));
			f = new File(currentPath + '/' + video.get(currentPath).get(position));
		}

		if(f.isDirectory()) {
			this.listView.setAdapter(new VideoListAdapter(this, video, currentPath));
			mActionBar.setTitle(name.get(position));
		} else {
			Intent i = new Intent(AppConstants.VIDEO_PLAY_ACTION);
			i.putStringArrayListExtra(AppConstants.VIDEO_PLAY_ACTION_LIST, video.get(currentPath));
			i.putExtra(AppConstants.VIDEO_PLAY_ACTION_PATH, currentPath);
			i.putExtra(AppConstants.VIDEO_PLAY_ACTION_INDEX, position);

			startActivity(i);
		}
	}

	/** 
	 * 작성자 : 임창민
	 * 메소드 이름 : onBackPressed()
	 * 매개변수 : 없음
	 * 반환값 : 없음
	 * 메소드 설명 : Android Back Key 리스너로 폴더에 있을때에는 루트경로로 오고 루트경로일때에는 앱을 종료시킨다.
	 */ 
	@Override
	public void onBackPressed() {
		if(!currentPath.equals(root)) {
			currentPath = root;
			listView.setAdapter(new VideoListAdapter(this, video, currentPath));
			mActionBar.setTitle("폴더");
		} else {
			finish();
		}
	}
}