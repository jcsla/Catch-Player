package com.ffmpegtest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ffmpegtest.adapter.MainAdapter;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnItemClickListener {

	private ActionBar mActionBar;
	private ListView mListView;
	private CursorAdapter mAdapter;
	private ArrayList<String> fileList = new ArrayList<String>();
	private String currentPath;
	private String moviesPath = "/storage/sdcard0/Movies/";
	private String cameraPath = "/storage/sdcard0/DCIM/Camera/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		setDefaultLayout();
	}
	
	public void setDefaultLayout()
	{
		currentPath = "default";
		
		MatrixCursor cursor = new MatrixCursor(MainAdapter.PROJECTION);
		cursor.addRow(new Object[] {
				1,
				"Movies",
				moviesPath});
		cursor.addRow(new Object[] {
				2,
				"Camera",
				cameraPath});
		
		mAdapter = new MainAdapter(this);
		mAdapter.swapCursor(cursor);

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle("폴더");
		
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			clickBackButton();
			break;
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.refresh:
			break;
		case R.id.search:
			break;
		}
		
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id)
	{
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String name = cursor.getString(MainAdapter.PROJECTION_NAME);
		
		if(name.compareTo("Movies") == 0)
		{
			currentPath = "Movies";
			openPath(moviesPath);
			updateAdapter(moviesPath);
		}
		else if(name.compareTo("Camera") == 0)
		{
			currentPath = "Camera";
			openPath(cameraPath);
			updateAdapter(cameraPath);
		}
		else
		{
			String path = cursor.getString(MainAdapter.PROJECTION_PATH);
			String fileName = cursor.getString(MainAdapter.PROJECTION_NAME);
			
			Intent intent = new Intent(AppConstants.VIDEO_PLAY_ACTION);
			intent.putExtra(AppConstants.VIDEO_PLAY_ACTION_PATH, path);
			intent.putExtra(AppConstants.VIDEO_PLAY_ACTION_NAME, fileName);
			
			startActivity(intent);
		}
	}
	
	public boolean openPath(String path)
	{
		fileList.clear();

		File file = new File(path);
		File[] files = file.listFiles();
		if (files == null)
			return false;
		
		for (int i = 0; i < files.length; i++)
		{
			if(files[i].isFile())
				fileList.add(files[i].getName());
		}
		
		Collections.sort(fileList);
		
		return true;
	}
	
	public void updateAdapter(String path)
	{
		MatrixCursor cursor = new MatrixCursor(MainAdapter.PROJECTION);
		
		for(int i=0 ; i<fileList.size() ; i++)
		{
			String fileName = fileList.get(i);
			String filePath = path + fileName;
			
			cursor.addRow(new Object[] {
				i,
				fileName,
				filePath});
		}
		
		mAdapter.swapCursor(cursor);
		
		mListView.setAdapter(mAdapter);
	}
	
	public void clickBackButton()
	{
		if(currentPath.compareTo("default") == 0)
			System.exit(0);
		else
			setDefaultLayout();
	}
}