package com.ffmpegtest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ffmpegtest.adapter.VideoFileDBAdapter;
import com.ffmpegtest.adapter.VideoListAdapter;
import com.ffmpegtest.adapter.FolderListAdapter;
import com.ffmpegtest.helpers.FFmpegCreateHelper;
import com.ffmpegtest.helpers.Util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;

public class MainActivity extends Activity implements OnItemClickListener,
		OnItemLongClickListener {

	private ActionBar mActionBar;
	private SearchView mSearchView;
	private HashMap<String, ArrayList<File>> video;
	private ListView listView;
	private String currentPath;
	private final String root = Environment.getExternalStorageDirectory()
			.getPath();
	private ArrayList<String> path;
	private ArrayList<Integer> videoLength;
	private VideoFileDBAdapter dbAdapter;
	private boolean isRoot;
	private Menu optionsMenu;
	public static String mFFmpegInstallPath;
	private Util util;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		listView = (ListView) findViewById(R.id.video_list);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);

		currentPath = Environment.getExternalStorageDirectory().getPath();
		video = new HashMap<String, ArrayList<File>>();
		//save_video = new HashMap<String, ArrayList<File>>();
		dbAdapter = new VideoFileDBAdapter(this);
		videoLength = new ArrayList<Integer>();
		util = Util.getInstance();

		installFFmpeg();

		initVideoMap();
	}
	
	/**
	 * 작성자 : 임창민 메소드 이름 : initVideoMap 매개변수 : 없음 반환값 : 없음 메소드 설명 : HashMap에 비디오
	 * 파일Path와 리스트를 초기화한다.
	 */
	public void initVideoMap() {
		video = dbAdapter.getVideoFileDB();
		if (video.size() == 0) {
			new RefreshTask().execute(root);
			setRefreshActionButtonState(true);
		} else {
			path = new ArrayList<String>(video.keySet());
			getVideoLength(path);
			setAdapter(new FolderListAdapter(this, path, videoLength));
		}
	}

	public void setAdapter(ListAdapter adapter) {
		if (adapter.getCount() == 0) {
			ArrayList<String> notData = new ArrayList<String>();
			notData.add("비디오가 없습니다.");
			listView.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, notData));
			listView.setOnItemClickListener(null);
			listView.setOnItemLongClickListener(null);
		} else {
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
		}
	}

	public void getVideoLength(ArrayList<String> folder) {
		videoLength.clear();
		for (int i = 0; i < folder.size(); i++) {
			videoLength.add(video.get(folder.get(i)).size());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position,
			long id) {
		if (!currentPath.equals(root) || isRoot) {
			playVideo(position);
		} else {
			currentPath = path.get(position);
			setAdapter(new VideoListAdapter(this, video.get(currentPath)));
			String[] split = currentPath.split("/");
			String title = split[split.length - 1];
			mActionBar.setTitle(title);
			if (currentPath.equals(root))
				isRoot = true;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			final int position, long arg3) {
		String items[];
		String fileName;
		DialogInterface.OnClickListener listener;
		if (!currentPath.equals(root) || isRoot) {
			items = new String[] { "재생", "삭제", "이름 변경", "속성" };
			fileName = util.removeExtension(video.get(currentPath).get(position).getName());
			listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						playVideo(position);
						break;
					case 1:
						deleteDirectory(video.get(currentPath).get(position));
						video.get(currentPath).remove(position);
						getVideoLength(path);
						setAdapter(new VideoListAdapter(MainActivity.this,
								video.get(currentPath)));
						break;
					case 2:
						renameFile(video.get(currentPath).get(position),
								position);
						break;
					case 3:
						showFileAttribute(video.get(currentPath).get(position),
								position);
						break;
					}
				}
			};
		} else {
			items = new String[] { "삭제", "이름 변경", "속성" };
			fileName = path.get(position);
			listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						deleteDirectory(new File(path.get(position)));
						video.remove(path.get(position));
						path.remove(position);
						getVideoLength(path);
						setAdapter(new FolderListAdapter(MainActivity.this,
								path, videoLength));
						break;
					case 1:
						renameFile(new File(path.get(position)), position);
						break;
					case 2:
						showFileAttribute(new File(path.get(position)),
								position);
						break;
					}
				}
			};
		}

		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(fileName);
		ab.setItems(items, listener);
		ab.show();
		return true;
	}

	public void renameFile(final File file, final int index) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("파일명을 입력해 주세요.");
		final EditText et_fileName = new EditText(this);
		
		String fileName;
		if(file.isDirectory())
			fileName = file.getName();
		else
			fileName = util.removeExtension(file.getName());
		
		et_fileName.setText(fileName);
		alert.setView(et_fileName);
		
		alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String fileName = et_fileName.getText().toString();
				if (!fileName.equals("")) {
					File newFile;
					
					if(file.isDirectory())
						newFile = new File(file.getParent() + '/' + fileName);
					else
						newFile = new File(file.getParent() + '/' + fileName + file.getName().substring(file.getName().lastIndexOf(".")));
					
					if (file.renameTo(newFile)) {
						if (!currentPath.equals(root) || isRoot) {
							video.get(currentPath).set(index, newFile);
							setAdapter(new VideoListAdapter(MainActivity.this,
									video.get(currentPath)));
						} else {
							ArrayList<File> tmp_list = new ArrayList<File>(
									video.get(path.get(index)));
							video.remove(path.get(index));
							video.put(newFile.getAbsolutePath(), tmp_list);
							path = new ArrayList<String>(video.keySet());
							setAdapter(new FolderListAdapter(MainActivity.this,
									path, videoLength));
						}
					}
				}
			}
		});

		alert.show();
	}

	public void showFileAttribute(File file, int position) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(file.getName());
		if (file.isDirectory()) {
			double videoSize = 0;
			for (File f : file.listFiles())
				videoSize += f.length();

			alert.setMessage("폴더\n"
					+ "\n위치 : "
					+ file.getAbsolutePath()
					+ "\n수정된 날짜 : "
					+ new SimpleDateFormat("MM/dd/yyyy").format(file
							.lastModified()) + "\n비디오 개수 : "
					+ videoLength.get(position) + "\n비디오 크기 : "
					+ util.getVideoSize(videoSize));
		} else {
			alert.setMessage("비디오파일\n"
					+ "\n이름 : "
					+ file.getName()
					+ "\n위치 : "
					+ file.getParent()
					+ "\n수정된 날짜 : "
					+ new SimpleDateFormat("MM/dd/yyyy").format(file
							.lastModified()) + "\n비디오 크기 : "
					+ util.getVideoSize(file.length()));
		}

		alert.setPositiveButton("확인", null);
		alert.show();
	}

	/**
	 * 작성자 : 임창민 메소드 이름 : getDir 매개변수 : String 반환값 : 없음 메소드 설명 : root부터 모든 path를
	 * 재귀호출하면서 동영상 파일을 찾는다.
	 */
	public void getDir(String str_path) {
		File f = new File(str_path);
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory() && !file.isHidden()
					&& !file.getPath().contains("/Android/data/"))
				getDir(file.getAbsolutePath());
			else {
				String fileName = file.getName();
				String filePath = file.getParent();
				if (util.isVideoFile(fileName)) {
					if (!video.containsKey(filePath)) {
						video.put(filePath, new ArrayList<File>());
						Log.e("newKey", filePath);
					}

					video.get(filePath).add(file);
				}
			}
		}
	}

	/**
	 * 작성자 : 임창민 메소드 이름 : isVideoFile 매개변수 : String 반환값 : boolean 메소드 설명 :
	 * Video파일인지 검사한다.
	 */

	public void playVideo(int position) {
		Intent i = new Intent(AppConstants.VIDEO_PLAY_ACTION);
		i.putStringArrayListExtra(AppConstants.VIDEO_PLAY_ACTION_LIST,
				getStringFileList(video.get(currentPath)));
		i.putExtra(AppConstants.VIDEO_PLAY_ACTION_INDEX, position);

		startActivity(i);
	}

	public ArrayList<String> getStringFileList(ArrayList<File> videoList) {
		ArrayList<String> stringList = new ArrayList<String>();
		for (int i = 0; i < videoList.size(); i++)
			stringList.add(videoList.get(i).getAbsolutePath());

		return stringList;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		getMenuInflater().inflate(R.menu.menu, menu);
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle("폴더");
		
		mSearchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		mSearchView.setQueryHint("비디오 검색");
		
		mSearchView
				.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
					
					@Override
					public boolean onQueryTextSubmit(String query) {
						ArrayList<String> findList = new ArrayList<String>();
						for (int i = 0; i < path.size(); i++) {
							ArrayList<File> tmp_list = video.get(path.get(i));
							for (int j = 0; j < tmp_list.size(); j++) {
								if (tmp_list.get(j).getName().contains(query))
									findList.add(tmp_list.get(j)
											.getAbsolutePath());
							}
						}

						Intent i = new Intent(MainActivity.this,
								FindActivity.class);
						i.putStringArrayListExtra("findList", findList);
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

		new RefreshTask().execute(root);
		setRefreshActionButtonState(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			setRefreshActionButtonState(true);
			new RefreshTask().execute(root);
			break;
		}

		return true;
	}

	/**
	 * 작성자 : 임창민 메소드 이름 : setRefreshActionButtonState 매개변수 : boolean 반환값 : 없음
	 * 메소드 설명 : ActionBar의 Refresh버튼을 Progress로 돌린다.
	 */
	public void setRefreshActionButtonState(final boolean refreshing) {
		if (optionsMenu != null) {
			final MenuItem refreshItem = optionsMenu
					.findItem(R.id.menu_refresh);
			if (refreshItem != null) {
				if (refreshing) {
					refreshItem
							.setActionView(R.layout.actionbar_indeterminate_progress);
				} else {
					refreshItem.setActionView(null);
				}
				refreshItem.expandActionView();
			}
		}
	}

	/**
	 * 작성자 : 임창민 메소드 이름 : onBackPressed() 매개변수 : 없음 반환값 : 없음 메소드 설명 : Android
	 * Back Key 리스너로 폴더에 있을때에는 루트경로로 오고 루트경로일때에는 앱을 종료시킨다.
	 */
	@Override
	public void onBackPressed() {
		if (!currentPath.equals(root) || isRoot) {
			if (video.get(currentPath).size() == 0) {
				video.remove(currentPath);
				path = new ArrayList<String>(video.keySet());
			}

			currentPath = root;
			getVideoLength(path);
			setAdapter(new FolderListAdapter(this, path, videoLength));
			mActionBar.setTitle("폴더");
			isRoot = false;
		} else {
			finish();
		}
	}

	public void initFinalize() {
//		video = new HashMap<String, ArrayList<File>>(save_video);
//		save_video.clear();
		path = new ArrayList<String>(video.keySet());
		getVideoLength(path);
		dbAdapter.removeVideoFileDB();
		for (int i = 0; i < path.size(); i++)
			dbAdapter.saveVideoFileDB(video.get(path.get(i)));
	}

	public boolean deleteDirectory(File path) {
		if (!path.exists()) {
			return false;
		}

		if (path.isDirectory()) {

			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (util.isVideoFile(files[i].getName())) {
					files[i].delete();

					Log.e(files[i].getName() + "삭제", files[i].getAbsolutePath());
				}
			}

		}

		return path.delete();
	}

	/**
	 * 작성자 : 임창민 클래스 이름 : RefreshTask 클래스 설명 : 디바이스의 디렉터리를 모두 검사해서 Video파일을 찾아내서
	 * 캐시에 저장한다.
	 */
	private class RefreshTask extends AsyncTask<String, Void, Boolean> {
		
		ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute() {
			pDialog = util.getProgress(MainActivity.this);
			pDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			video.clear();
			getDir(params[0]);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			setRefreshActionButtonState(false);

			initFinalize();
			pDialog.dismiss();
			// if(!path.contains(currentPath)) {
			// currentPath = root;
			// mActionBar.setTitle("폴더");
			// }

			if (!currentPath.equals(root) || isRoot)
				setAdapter(new VideoListAdapter(MainActivity.this,
						video.get(currentPath)));
			else
				setAdapter(new FolderListAdapter(MainActivity.this, path,
						videoLength));
		}
	}

	/**
	 * 작성자 : 이준영 메소드 이름 : installFFmpeg 메소드 설명 : ffmpeg 실행파일을 로컬 디바이스에 설치한다.
	 *
	 */
	public void installFFmpeg()
	{
		File ffmpegFile = new File(getCacheDir(), "ffmpeg");
		mFFmpegInstallPath = ffmpegFile.toString();

		System.out.println("path : " + mFFmpegInstallPath);

		if (!ffmpegFile.exists()) {
			try {
				ffmpegFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FFmpegCreateHelper.installBinaryFromRaw(this, R.raw.ffmpeg, ffmpegFile);
		ffmpegFile.setExecutable(true);
	}
}