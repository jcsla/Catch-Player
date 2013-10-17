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
import java.util.Collection;
import java.util.HashMap;

import com.ffmpegtest.adapter.VideoFileDBAdapter;
import com.ffmpegtest.adapter.VideoListAdapter;
import com.ffmpegtest.adapter.FolderListAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SearchView;

public class MainActivity extends Activity implements OnItemClickListener {

	private ActionBar mActionBar;
	private SearchView mSearchView;
	private HashMap<String, ArrayList<VideoFile>> video, save_video;
	private ListView listView;
	private String currentPath;
	private String root;
	private ArrayList<String> path;
	private ArrayList<Integer> videoLength;
	private VideoFileDBAdapter dbAdapter;
	private boolean inRoot;
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
		listView = (ListView)findViewById(R.id.video_list);
		listView.setOnItemClickListener(this);
		//listView.setOnItemLongClickListener(this);

		currentPath = Environment.getExternalStorageDirectory().getPath();
		root = Environment.getExternalStorageDirectory().getPath();
		video = new HashMap<String, ArrayList<VideoFile>>();
		save_video = new HashMap<String, ArrayList<VideoFile>>();
		dbAdapter = new VideoFileDBAdapter(this);
		videoLength = new ArrayList<Integer>();

		initVideoMap();

	}

	/** 
	 * 작성자 : 임창민
	 * 메소드 이름 : initVideoMap
	 * 매개변수 : 없음
	 * 반환값 : 없음
	 * 메소드 설명 : HashMap에 비디오 파일Path와 리스트를 초기화한다.
	 */ 
	public void initVideoMap() {
		save_video = dbAdapter.getVideoFileDB();
		if(save_video.size() == 0) {
			new RefreshTask().execute(root);
			setRefreshActionButtonState(true);
		} else {
			initFinalize();
			listView.setAdapter(new FolderListAdapter(this, path, videoLength)); 
		}
	}

	public void getVideoLength(ArrayList<String> folder) {
		videoLength.clear();
		for(int i=0; i<folder.size(); i++) {
			videoLength.add(video.get(folder.get(i)).size());
		}
	}

	public ArrayList<String> getVideoFileList(int position) {
		ArrayList<String> fileList = new ArrayList<String>();
		for(int i=0; i<videoLength.get(position); i++) {
			String filePath = getfilePath(video.get(path.get(position)).get(i));
			fileList.add(filePath);
		}

		return fileList;
	}

	public String getfilePath(VideoFile videoFile) {
		return videoFile.getPath() + '/' + videoFile.getName();
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id)
	{
		if(currentPath.equals(root) && !inRoot) {
			currentPath = path.get(position);
			this.listView.setAdapter(new VideoListAdapter(this, video.get(currentPath)));
			String[] split = currentPath.split("/");
			String title = split[split.length - 1];
			mActionBar.setTitle(title);
			if(currentPath.equals(root))
				inRoot = true;
		} else {
			playVideo(position);
		}
	}

	//	@Override
	//	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
	//			final int position, long arg3) {
	//		final String items[] = { "재생", "삭제", "이름 변경", "속성" };
	//		String fileName = video.get(currentPath).get(position).getName();
	//		AlertDialog.Builder ab = new AlertDialog.Builder(this);
	//		ab.setTitle(fileName);
	//		ab.setItems(items, new DialogInterface.OnClickListener() {
	//
	//			@Override
	//			public void onClick(DialogInterface dialog, int which) {
	//				switch (which) {
	//				case 0:
	//					playVideo(position);
	//					break;
	//				case 1:
	//					break;
	//				case 2:
	//					break;
	//				case 3:
	//					break;
	//				}
	//			}
	//		});
	//		ab.show();
	//		return true;
	//	}

	/** 
	 * 작성자 : 임창민
	 * 메소드 이름 : getDir
	 * 매개변수 : String
	 * 반환값 : 없음
	 * 메소드 설명 : root부터 모든 path를 재귀호출하면서 동영상 파일을 찾는다.
	 */ 
	public void getDir(String str_path) {
		File f = new File(str_path);
		File[] files = f.listFiles();
		for(int i=0; i<files.length; i++) {
			File file = files[i];
			if(file.isDirectory() && !file.isHidden() && !file.getPath().contains("/Android/data/"))
				getDir(file.getAbsolutePath());
			else {
				String fileName = file.getName();
				String filePath = file.getParent();
				if(isVideoFile(fileName)) {
					if(!save_video.containsKey(filePath)) {
						save_video.put(filePath, new ArrayList<VideoFile>());
						Log.e("newKey", filePath);
					}

					boolean timeFlag = false;

					if(video != null && video.containsKey(filePath)) {
						ArrayList<VideoFile> videoList = video.get(filePath);
						for(int j=0; j<videoList.size(); j++) {
							if(videoList.get(j).getName().equals(fileName)) {
								save_video.get(filePath).add(new VideoFile(filePath, fileName, videoList.get(j).getTime(), videoList.get(j).getNew_video()));
								timeFlag = true;
							}
						}
					}

					if(timeFlag == false)
						save_video.get(filePath).add(new VideoFile(filePath, fileName, 0, 1));
					Log.e("addValue : " , file.getAbsolutePath());
				}
			}
		}
	}

	/** 
	 * 작성자 : 임창민
	 * 메소드 이름 : isVideoFile
	 * 매개변수 : String
	 * 반환값 : boolean
	 * 메소드 설명 : Video파일인지 검사한다.
	 */ 
	public static boolean isVideoFile(String file){
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

	public void playVideo(int position) {
		Intent i = new Intent(AppConstants.VIDEO_PLAY_ACTION);
		i.putParcelableArrayListExtra(AppConstants.VIDEO_PLAY_ACTION_LIST, video.get(currentPath));
		i.putExtra(AppConstants.VIDEO_PLAY_ACTION_INDEX, position);

		startActivityForResult(i, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		video = dbAdapter.getVideoFileDB();
		listView.setAdapter(new VideoListAdapter(MainActivity.this, video.get(currentPath)));
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
				ArrayList<VideoFile> findList = new ArrayList<VideoFile>();
				for(int i=0; i<path.size(); i++) {
					ArrayList<VideoFile> tmp_list = video.get(path.get(i));
					for(int j=0; j<tmp_list.size(); j++) {
						if(tmp_list.get(j).getName().contains(query))
							findList.add(tmp_list.get(j));
					}
				}

				Intent i = new Intent(MainActivity.this, FindActivity.class);
				i.putExtra("findList", findList);
				i.putExtra("findQuery", query);
				startActivity(i);

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				Log.e("체인지", newText);
				return false;
			}
		});

		//		new RefreshTask().execute(root);
		//		setRefreshActionButtonState(true);
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


	/** 
	 * 작성자 : 임창민
	 * 메소드 이름 : setRefreshActionButtonState
	 * 매개변수 : boolean
	 * 반환값 : 없음
	 * 메소드 설명 : ActionBar의 Refresh버튼을 Progress로 돌린다.
	 */ 
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
			listView.setAdapter(new FolderListAdapter(this, path, videoLength));
			mActionBar.setTitle("폴더");
			inRoot = false;
		} else {
			finish();
		}
	}

	public void initFinalize() {
		video = new HashMap<String, ArrayList<VideoFile>>(save_video);
		save_video.clear();
		path = new ArrayList<String>(video.keySet());
		getVideoLength(path);
		dbAdapter.removeVideoFileDB();
		for(int i=0; i<path.size(); i++)
			dbAdapter.saveVideoFileDB(video.get(path.get(i)));
	}

	/** 
	 * 작성자 : 임창민
	 * 클래스 이름 : RefreshTask
	 * 클래스 설명 : 디바이스의 디렉터리를 모두 검사해서 Video파일을 찾아내서 캐시에 저장한다.
	 */ 
	private class RefreshTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			getDir(params[0]);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			setRefreshActionButtonState(false);
			
			initFinalize();
			if(currentPath.equals(root) && !inRoot)
				listView.setAdapter(new FolderListAdapter(MainActivity.this, path, videoLength));
			else
				listView.setAdapter(new VideoListAdapter(MainActivity.this, video.get(currentPath)));
		}
	}
}