package com.ffmpegtest;

import java.util.ArrayList;

import com.ffmpegtest.adapter.VideoListAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

public class FindActivity extends Activity {
	
	private ListView mVideoListView;
	private ArrayList<String> mVideoList;
	private ActionBar mActionBar;
	private String find_str;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		init();
		mVideoListView.setAdapter(new VideoListAdapter(this, mVideoList));
	}
	
	public void init() {
		mVideoList = getIntent().getStringArrayListExtra("video");
		mVideoListView = (ListView)findViewById(R.id.video_list);
		find_str = getIntent().getStringExtra("find");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(find_str);
		
		return true;
	}
}
