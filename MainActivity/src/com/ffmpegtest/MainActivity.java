package com.ffmpegtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ffmpegtest.adapter.VideoListAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;

public class MainActivity extends Activity implements OnItemClickListener {

	private ActionBar mActionBar;
	private SearchView mSearchView;
	private HashMap<String, ArrayList<String>> video, save_video;
	private ArrayList<String> path;
	private ArrayList<String> name;
	private ListView listView;
	private String currentPath;
	private String root;
	boolean inRoot = false;
	private Menu optionsMenu;
	public static final String supportedVideoFileFormats[] = 
		{   "mp4","wmv","avi","mkv","dv",
		"rm","mpg","mpeg","flv","divx",
		"swf","h264","h263","h261",
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
		save_video = new HashMap<String, ArrayList<String>>();

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
		
		try {
			InputStream in = openFileInput("cache.dat"); 
			video.clear();
			if(in != null){

				InputStreamReader input = new InputStreamReader(in); 
				BufferedReader reader = new BufferedReader(input); 

				String line;
				while((line = reader.readLine()) != null){
					String[] split = line.split(";");
					Log.e("line : ", line);
					Log.e("up = " + split[0], "filePath = " + split[1]);
					Log.e("fileName = " + split[2], "재생시간 = " + split[3]);
					if(!video.containsKey(split[1]))
						video.put(split[1], new ArrayList<String>());
					video.get(split[1]).add(split[2]);
				}
				input.close(); 
			}     
			in.close();
		} catch (IOException e) {
			getDir(root);
			saveCache();
			video = new HashMap<String, ArrayList<String>>(save_video);
			save_video.clear();
		}
		
		path = new ArrayList<String>(video.keySet());
		for(int i=0; i<path.size(); i++) {
			String[] pathList = path.get(i).split("/");
			name.add(pathList[pathList.length - 1]);
		}
		listView.setAdapter(new VideoListAdapter(this, video, currentPath)); 
	}

	public void getDir(String str_path) {
		File f = new File(str_path);
		File[] files = f.listFiles();
		for(int i=0; i<files.length; i++) {
			File file = files[i];
			if(file.isDirectory() && !file.isHidden() && !file.getPath().contains("/Android/data/"))
				getDir(file.getAbsolutePath());
			else {
				if(isVideoFile(file.getName())) {
					if(!save_video.containsKey(file.getParent())) {
						save_video.put(file.getParent(), new ArrayList<String>());
						Log.e("newKey", file.getParent());
					}

					save_video.get(file.getParent()).add(file.getName());
					Log.e("addValue : " , file.getAbsolutePath());
				}
			}
		}
	}
	
	public boolean saveCache() {
		try {
			OutputStream out = openFileOutput("cache.dat", MODE_PRIVATE); 
			if(out != null){
				OutputStreamWriter output = new OutputStreamWriter(out); 
				ArrayList<String> key = new ArrayList<String>(save_video.keySet());
				for(int i=0; i<key.size(); i++) {
					for(int j=0; j<save_video.get(key.get(i)).size(); j++) {
						output.write("0;" + key.get(i) + ';' + save_video.get(key.get(i)).get(j) + ';' + "0:00:00" + '\n');
					}
				}
				output.close(); 
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
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
		this.optionsMenu = menu;
		getMenuInflater().inflate(R.menu.menu, menu);

		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle("폴더");

		mSearchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
		mSearchView.setQueryHint("비디오 검색");
		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				ArrayList<String> key = new ArrayList<String>(video.keySet());

				for(int i=0; i<key.size(); i++) {
					ArrayList<String> value = video.get(key.get(i));
					for(int j=0; j<value.size(); j++) {
						String file = key.get(i) + '/' + value.get(j);
						if(file.contains(query.replace(root, "")))
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

		new RefreshTask().execute(root);
		setRefreshActionButtonState(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_refresh:
			setRefreshActionButtonState(true);
			new RefreshTask().execute(root);
			break;
		}

		return true;
	}
	
	public void setRefreshActionButtonState(final boolean refreshing) {
		if (optionsMenu != null) {
	        final MenuItem refreshItem = optionsMenu
	            .findItem(R.id.menu_refresh);
	        if (refreshItem != null) {
	            if (refreshing) {
	                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
	            } else {
	                refreshItem.setActionView(null);
	            }
				refreshItem.expandActionView();
	        }
	    }
	}
	
	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id)
	{
		File f = null;
		if(currentPath.equals(root) && !inRoot) {
			f = new File(path.get(position));
			currentPath = f.getAbsolutePath();
			if(currentPath.equals(root)) {
				this.listView.setAdapter(new VideoListAdapter(this, video, currentPath, true));
				inRoot = true;
			}
		}

		else {
			Log.e("currentPath : ", currentPath);
			Log.e("name : ", video.get(currentPath).get(position));
			f = new File(currentPath + '/' + video.get(currentPath).get(position));
		}

		if(f.isDirectory() && !inRoot) {
			this.listView.setAdapter(new VideoListAdapter(this, video, currentPath));
			mActionBar.setTitle(name.get(position));
		} else if(f.isFile()){
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
		if(!currentPath.equals(root) || inRoot) {
			currentPath = root;
			listView.setAdapter(new VideoListAdapter(this, video, currentPath));
			mActionBar.setTitle("폴더");
			inRoot = false;
		} else {
			finish();
		}
	}
	
	private class RefreshTask extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... params) {
			getDir(params[0]);
			saveCache();
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			setRefreshActionButtonState(false);
			video = new HashMap<String, ArrayList<String>>(save_video);
			save_video.clear();
			path = new ArrayList<String>(video.keySet());
			for(int i=0; i<path.size(); i++) {
				String[] pathList = path.get(i).split("/");
				name.add(pathList[pathList.length - 1]);
			}
			listView.setAdapter(new VideoListAdapter(MainActivity.this, video, currentPath, inRoot)); 
		}
	}
}