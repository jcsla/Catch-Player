package com.ffmpegtest;

import java.io.File;
import java.util.ArrayList;

import com.ffmpegtest.adapter.VideoFileDBAdapter;
import com.ffmpegtest.adapter.VideoListAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FindActivity extends Activity implements OnItemClickListener {
	
	private ListView mVideoListView;
	private ArrayList<String> mVideoList;
	private VideoFileDBAdapter dbAdapter;
	private ActionBar mActionBar;
	private String find_str;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		init();
		if(mVideoList.size() == 0) {
			mVideoListView.setSelector(new PaintDrawable(0xffffff));
			mVideoListView.setDivider(null);
			ArrayList<String> notData = new ArrayList<String>();
			notData.add("검색결과가 없습니다.");
			mVideoListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notData));
		} else {
			mVideoListView.setOnItemClickListener(this);
			mVideoListView.setAdapter(new VideoListAdapter(this, getFileList(mVideoList)));
		}
	}
	
	public ArrayList<File> getFileList(ArrayList<String> stringList) {
		ArrayList<File> fileList = new ArrayList<File>();
		for(int i=0; i<stringList.size(); i++) 
			fileList.add(new File(stringList.get(i)));
		
		return fileList;
	}
	
	public void init() {
		mVideoList = getIntent().getStringArrayListExtra("findList");
		mVideoListView = (ListView)findViewById(R.id.video_list);
		dbAdapter = new VideoFileDBAdapter(this);
		find_str = getIntent().getStringExtra("findQuery");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(find_str);
		
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Intent i = new Intent(AppConstants.VIDEO_PLAY_ACTION);
		i.putStringArrayListExtra(AppConstants.VIDEO_PLAY_ACTION_LIST, mVideoList);
		i.putExtra(AppConstants.VIDEO_PLAY_ACTION_INDEX, position);

		startActivity(i);
	}
}
