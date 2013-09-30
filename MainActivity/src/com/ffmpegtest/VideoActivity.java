/*
 * MainActivity.java
 * Copyright (c) 2012 Jacek Marchwicki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ffmpegtest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;

public class VideoActivity extends Activity implements FFmpegListener, OnClickListener, OnSeekBarChangeListener, OnTouchListener
{
	private FFmpegPlayer mMpegPlayer;
	protected boolean mPlay = false;
	ArrayList<String> videoList;

	private View mFullLayout;
	private boolean mTouchPressed = false;

	private View mTitleBar;
	private TextView mTitle;

	private View mVideoView;

	private View mControlsView;
	private SeekBar mSeekBar;
	private ImageButton mPlayPauseButton;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private ImageView mPPLButton;
	private ListView mPPLList;

	private boolean mTracking = false;

	private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
	private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
	
	String path;
	private String fileName;
	private int index;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DITHER);

		super.onCreate(savedInstanceState);

		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().setBackgroundDrawable(null);

		setContentView(R.layout.video_surfaceview);

		mFullLayout = this.findViewById(R.id.full_layout);
		mFullLayout.setOnTouchListener(this);

		mTitleBar = this.findViewById(R.id.title_bar);
		mTitle = (TextView) this.findViewById(R.id.title);

		mControlsView = this.findViewById(R.id.controls);

		mSeekBar = (SeekBar) this.findViewById(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(this);

		mPlayPauseButton = (ImageButton) this.findViewById(R.id.play_pause);
		mPlayPauseButton.setOnClickListener(this);

		mPPLButton = (ImageView) this.findViewById(R.id.btn_ppl);
		mPPLButton.setOnClickListener(this);

		mCurrentTime = (TextView) this.findViewById(R.id.current_time);
		mTotalTime = (TextView) this.findViewById(R.id.total_time);

		mVideoView = this.findViewById(R.id.video_view);

		mPPLList = (ListView) this.findViewById(R.id.lv_ppl);
		ViewGroup.LayoutParams params = mPPLList.getLayoutParams();
		params.width = (getDeviceWidth() / 2);
		params.height = LayoutParams.MATCH_PARENT;
		mPPLList.setLayoutParams(params);
		ArrayList<String> list = new ArrayList<String>();
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		list.add("가나다라");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		mPPLList.setAdapter(adapter);

		mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
		mMpegPlayer.setMpegListener(this);

		setDataSource();

		mMpegPlayer.resume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		this.mMpegPlayer.setMpegListener(null);
		this.mMpegPlayer.stop();
		stop();
	}

	private void setDataSource()
	{
		HashMap<String, String> params = new HashMap<String, String>();

		// set font for ass
		File assFont = new File(Environment.getExternalStorageDirectory(), "DroidSansFallback.ttf");
		params.put("ass_default_font_path", assFont.getAbsolutePath());

		if(videoList == null) {
			Intent intent = getIntent();
			Uri uri = intent.getData();

			if (uri != null)
			{
				path = uri.toString();
			}
			else
			{
				videoList = intent.getStringArrayListExtra(AppConstants.VIDEO_PLAY_ACTION_LIST);
				path = intent.getStringExtra(AppConstants.VIDEO_PLAY_ACTION_PATH);
				index = intent.getIntExtra(AppConstants.VIDEO_PLAY_ACTION_INDEX, 0);
				fileName = videoList.get(index);
			}
		}

		mTitle.setText(fileName);

		this.mPlayPauseButton.setImageResource(R.drawable.pause);
		this.mPlayPauseButton.setEnabled(true);

		mPlay = true;
		mTouchPressed = false;

		mMpegPlayer.setDataSource(path + ('/' + fileName), params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo, mSubtitleStreamNo);
		Log.e("filePath : ", path + ('/' + fileName));
	}

	public void nextVideo() {
		if(videoList != null && index < videoList.size() - 1) {
			fileName = videoList.get(++index);
			setDataSource();
			mMpegPlayer.resume();
		}
	}

	public void prevVideo() {
		if(index > 0) {
			fileName = videoList.get(--index);
			setDataSource();
			mMpegPlayer.resume();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			if(mPPLList.getVisibility() == View.GONE) {
				if(mTouchPressed == false)
				{
					mTouchPressed = true;
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					this.mTitleBar.setVisibility(View.VISIBLE);
					this.mControlsView.setVisibility(View.VISIBLE);
					this.mPPLButton.setVisibility(View.VISIBLE);
				}
				else
				{
					mTouchPressed = false;
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					this.mTitleBar.setVisibility(View.GONE);
					this.mControlsView.setVisibility(View.GONE);
					this.mPPLButton.setVisibility(View.GONE);
				}
			} else {
				mPPLList.setVisibility(View.GONE);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.play_pause:
			resumePause();
			Log.e("Stop","Play");
			break;
		case R.id.next_video:
			nextVideo();
			break;
		case R.id.prev_video:
			prevVideo();
			break;
		case R.id.btn_ppl:
			mPPLList.setVisibility(View.VISIBLE);
			break;
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		String value = String.valueOf(seekBar.getProgress());
		if (fromUser)
		{
			long timeUs = Long.parseLong(value) * 1000 * 1000;
			int currentTimeS = (int)(timeUs / 1000 / 1000);
			mCurrentTime.setText(parseTime(currentTimeS));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		mTracking = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		mTracking = false;

		String value = String.valueOf(seekBar.getProgress());
		//if (fromUser)
		//{
		//System.out.println(seekBar.getProgress());
		//long timeUs = Long.parseLong(value) * 1000 * 1000;
		//System.out.println("timeUs = " + timeUs);
		mMpegPlayer.seek(value);
		//}
	}

	@Override
	public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams)
	{
		if (err != null)
		{
			String format = getResources().getString(
					R.string.main_could_not_open_stream);
			String message = String.format(format, err.getMessage());

			Builder builder = new AlertDialog.Builder(VideoActivity.this);
			builder.setTitle(R.string.app_name)
			.setMessage(message)
			.setOnCancelListener(
					new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							VideoActivity.this.finish();
						}
					}).show();
			return;
		}
		mPlayPauseButton.setEnabled(true);
	}

	@Override
	public void onFFUpdateTime(long currentTimeUs, long videoDurationUs, boolean isFinished)
	{
		int currentTimeS = (int)(currentTimeUs / 1000 / 1000);
		int videoDurationS = (int)(videoDurationUs / 1000 / 1000);
		mSeekBar.setMax(videoDurationS);
		mSeekBar.setProgress(currentTimeS);

		mCurrentTime.setText(parseTime(currentTimeS));
		mTotalTime.setText(parseTime(videoDurationS));

		if (isFinished)
		{
			mSeekBar.setProgress(0);

			// 다음 동영상 재생 여부 확인
			// ok이면 다음 동영상으로 넘어감.

			//new AlertDialog.Builder(this)
			//		.setTitle(R.string.dialog_end_of_video_title)
			//		.setMessage(R.string.dialog_end_of_video_message)
			//		.setCancelable(true).show();
		}
	}

	@Override
	public void onFFResume(NotPlayingException result)
	{
		this.mPlayPauseButton.setImageResource(R.drawable.pause);
		this.mPlayPauseButton.setEnabled(true);

		displaySystemMenu(false);

		mPlay = true;
	}

	@Override
	public void onFFPause(NotPlayingException err)
	{
		this.mPlayPauseButton.setImageResource(R.drawable.play);
		this.mPlayPauseButton.setEnabled(true);
		mPlay = false;
	}

	@Override
	public void onFFStop()
	{

	}

	@Override
	public void onFFSeeked(NotPlayingException result)
	{
		//if (result != null)
		//	throw new RuntimeException(result);
	}

	private void displaySystemMenu(boolean visible) {
		if (Build.VERSION.SDK_INT >= 14) {
			displaySystemMenu14(visible);
		} else if (Build.VERSION.SDK_INT >= 11) {
			displaySystemMenu11(visible);
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(11)
	private void displaySystemMenu11(boolean visible) {
		if (visible) {
			this.mVideoView.setSystemUiVisibility(View.STATUS_BAR_VISIBLE);
		} else {
			this.mVideoView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}
	}

	@TargetApi(14)
	private void displaySystemMenu14(boolean visible) {
		if (visible) {
			this.mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		} else {
			this.mVideoView
			.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

	public void resumePause()
	{
		this.mPlayPauseButton.setEnabled(false);

		if (mPlay)
		{
			mMpegPlayer.pause();
		}
		else
		{
			mMpegPlayer.resume();
			displaySystemMenu(true);
		}

		mPlay = !mPlay;
	}

	private void stop()
	{
		this.mControlsView.setVisibility(View.GONE);
	}

	/**
	 * 작성자 : 이준영
	 * 메소드 이름 : parseTIme
	 * 매개변수 : 가공되지 않은 시간
	 * 반환값 : 가공된 시간
	 * 메소드 설명 : 시간을 매개변수로 받아 처리해서 반환해준다(TextView에 적절하게 뿌리기 위해)
	 */
	private String parseTime(int time)
	{
		String minS = null;
		String secS = null;
		int total = time;
		int spare;

		int hour = total / (60 * 60);
		spare = total % (60 * 60);
		int min = spare / (60);
		spare = spare % (60);
		int sec = spare;

		if (min < 10)
			minS = "0" + min;
		else
			minS = min + "";
		if (sec < 10)
			secS = "0" + sec;
		else
			secS = sec + "";

		String result = hour + " : " + minS + " : " + secS;

		return result;
	}
	
	private int getDeviceWidth() {
		if (12 < Build.VERSION.SDK_INT) {
			Point p = new Point();
			getWindowManager().getDefaultDisplay().getSize(p);
			return p.x;
		} else {
			return getWindowManager().getDefaultDisplay().getWidth();
		}
	}
}