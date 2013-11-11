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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.audiofx.BassBoost.Settings;
import android.net.Uri;
import android.net.rtp.AudioStream;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings.SettingNotFoundException;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;
import com.ffmpegtest.adapter.PPLListAdapter;
import com.ffmpegtest.adapter.VideoFileDBAdapter;
import com.ffmpegtest.helpers.AudioFingerPrintHelper;
import com.ffmpegtest.helpers.Util;

public class VideoActivity extends Activity implements FFmpegListener, OnClickListener, OnSeekBarChangeListener, OnTouchListener, View.OnSystemUiVisibilityChangeListener
{
	public static FFmpegPlayer mMpegPlayer;
	protected boolean mPlay = false;

	private View mFullLayout;
	private boolean mTouchPressed = false;
	private Util util = Util.getInstance();

	private View mTitleBar;
	private TextView mTitle;
	private TextView mSmiview;

	private View mVideoView;

	private View mSeekControlView;
	private TextView mSeekControlValue;
	private TextView mSeekControlSmallValue;
	private View mControlsView;
	private SeekBar mSeekBar;
	private ImageButton mPlayPauseButton;
	private ImageButton mHoldButton;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private int currentTimeS;

	private View mVolumeBrightnessControlView;
	private TextView mVolumeBrightnessValue;
	private float brightnessValue;
	private Boolean brightnessCheck;

	private Handler mSeekControlHandler;
	private Handler mControllerHandler;
	private Handler mHoldHandler;

	private ImageView mPPLButton;
	private ListView mPPLList;
	private RelativeLayout mPPLLayout;

	private SlidingDrawer drawer;

	private View mUnHoldButtonView;
	private ImageButton mUnHoldButton;
	private Boolean holdCheck;

	private Button mSlideButton;

	private AudioManager mAudioManager;
	private int mAudioMax;
	private float mVolume;

	private boolean onPPL = false;
	private boolean mHold = false;
	private boolean mMove = false;
	private boolean mSeek = false;
	private boolean isFinish = false;
	private boolean mUseSubtitle = false;
	private int seekValue;
	private float mTouchX;
	private float mTouchY;
	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_4_3 = 1;
	private static final int SURFACE_16_9 = 2;
	private int mCurrentSize = SURFACE_BEST_FIT;

	private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
	private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;

	ArrayList<String> videoList;
	ArrayList<SubtitleData> parsedSubtitleDataList;

	private File file;
	public static String path;
	private int index;
	private int indexSubtitle;
	private long currentTime;

	private VideoFileDBAdapter dbAdapter;
	public static ProgressDialog progess;



	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		System.out.println("VideoActivity create!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DITHER);

		Log.e("Flag", "onCreate");
		super.onCreate(savedInstanceState);

		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().setBackgroundDrawable(null);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.video_surfaceview);

		mFullLayout = this.findViewById(R.id.full_layout);
		mFullLayout.setOnTouchListener(this);
		mFullLayout.setOnSystemUiVisibilityChangeListener(this);

		mTitleBar = this.findViewById(R.id.title_bar);
		mTitle = (TextView) this.findViewById(R.id.title);

		mControlsView = this.findViewById(R.id.controls);

		mVolumeBrightnessControlView = this.findViewById(R.id.volume_brightness_control);
		mVolumeBrightnessValue = (TextView)this.findViewById(R.id.volume_brightness_value);

		mSeekControlView = this.findViewById(R.id.seek_control);
		mSeekControlValue = (TextView)this.findViewById(R.id.seek_value);
		mSeekControlSmallValue = (TextView)this.findViewById(R.id.seek_small_value);

		mSeekBar = (SeekBar) this.findViewById(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(this);

		mPlayPauseButton = (ImageButton) this.findViewById(R.id.play_pause);
		mPlayPauseButton.setOnClickListener(this);

		mHoldButton = (ImageButton) this.findViewById(R.id.hold_video);
		mHoldButton.setOnClickListener(this);

		mPPLButton = (ImageView) this.findViewById(R.id.btn_ppl);
		mPPLButton.setOnClickListener(this);

		drawer = (SlidingDrawer)findViewById(R.id.slide);

		mSlideButton = (Button)this.findViewById(R.id.btn_slide);
		mSlideButton.setOnClickListener(this);

		mCurrentTime = (TextView) this.findViewById(R.id.current_time);
		mTotalTime = (TextView) this.findViewById(R.id.total_time);

		mVideoView = this.findViewById(R.id.video_view);

		mPPLList = (ListView) this.findViewById(R.id.lv_ppl);

		mPPLLayout = (RelativeLayout) this.findViewById(R.id.ll_ppl);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAlpha(160);
		mPPLLayout.setBackgroundColor(paint.getColor());

		mUnHoldButtonView = this.findViewById(R.id.unhold_area);
		mUnHoldButton = (ImageButton) this.findViewById(R.id.unhold_button);
		mUnHoldButton.setOnClickListener(this);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		dbAdapter = new VideoFileDBAdapter(this);

		//currentTimeS =0;

		//홀드버튼
		holdCheck = true;
		//                IntentFilter offFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		//                registerReceiver(screenoff, offFilter);
		brightnessCheck = false;

		/*mVideoView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if(visibility != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION){
					Toast.makeText(getApplicationContext(), "a;slkdjfalksj", Toast.LENGTH_SHORT).show();



				}
			}
		});*/

		if(android.os.Build.VERSION.SDK_INT>9){
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		/*LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        for (int i = 1; i < 3; i++) {
            ImageView imageView = new ImageView(this);

            URL url;
			try {
				Log.e("ImageParser", ""+JSONParserHelper.product_image);
				url = new URL(JSONParserHelper.product_image);
				Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				imageView.setImageBitmap(bmp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("ImageParser", "Fuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuck");
			}

            //imageView.setImageResource();
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layout.addView(imageView, p);
        }
		 */




		doBrightnessTouch(0.0f);

		ViewGroup.LayoutParams params = mPPLList.getLayoutParams();
		params.width = (getDeviceWidth() / 2);
		params.height = LayoutParams.MATCH_PARENT;
		mPPLList.setLayoutParams(params);
		mPPLList.setBackgroundColor(paint.getColor());
		ArrayList<String> list = new ArrayList<String>();
		list.add("짱좋은옷");
		list.add("짱좋은가방");
		list.add("짱좋은신발");
		list.add("짱멋진우산");
		list.add("완전쩌는스카프");
		list.add("대박멋진목걸이");
		list.add("그냥그런바지");
		list.add("귀티나는양말");
		list.add("이상한겉옷");
		list.add("담요");
		PPLListAdapter adapter = new PPLListAdapter(this, list);
		mPPLList.setAdapter(adapter);

		mMpegPlayer = null;
		mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
		mMpegPlayer.setMpegListener(this);

		setDataSource();

		progess = util.getProgress(this);

		AudioFingerPrintHelper.startAudioFingerPrint();


		mMpegPlayer.resume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(holdCheck == true){
			menu.add(0, 0, Menu.NONE, "설정").setIcon(android.R.drawable.ic_menu_preferences);
			menu.add(0, 1, Menu.NONE, "도움말").setIcon(android.R.drawable.ic_menu_help);

			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 0:
			break;
		case 1:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause()
	{
		Log.e("Flag", "beforeSuperPause");
		super.onPause();
		mPlay = false;
		Log.e("Flag", "beforePause");
		mMpegPlayer.pause();
		Log.e("Flag", "afterPause");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Log.e("Flag", "onResume");
	}

	@Override
	protected void onDestroy()
	{
		Log.e("Flag", "onDestroy");
		this.mMpegPlayer.setMpegListener(null);
		this.mMpegPlayer.stop();
		stop();
		super.onDestroy();
		//unregisterBroadcast();
	}

	/*        private void unregisterBroadcast()
        {
                unregisterReceiver(screenoff);
        }

        BroadcastReceiver screenoff = new BroadcastReceiver(){
                public static final String Screenoff = "android.intent.action.SCREEN_OFF";

                public void onReceive(android.content.Context context, Intent intent) {
                        if(!intent.getAction().equals(Screenoff))return;

                        if(holdCheck == true){
                                Intent holdIntent = new Intent(Intent.ACTION_SCREEN_ON);
                                startActivity(holdIntent);
                        }else if(holdCheck == false){
                                mMpegPlayer.setMpegListener(null);
                                mMpegPlayer.stop();
                                stop();
                        }

                }

        };*/

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
				index = intent.getIntExtra(AppConstants.VIDEO_PLAY_ACTION_INDEX, 0);
				file = new File(videoList.get(index));
				path = file.getAbsolutePath();
			}
		}

		String[] split = path.split("/");
		String title = split[split.length - 1];
		mTitle.setText(title);

		this.mPlayPauseButton.setImageResource(R.drawable.pause);
		this.mPlayPauseButton.setEnabled(true);

		mPlay = true;
		mTouchPressed = false;
		mHold = false;

		int time = dbAdapter.getVideoTime(path);

		mMpegPlayer.setDataSource(path, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo, mSubtitleStreamNo);
		if(time > 0)
			mMpegPlayer.seek(String.valueOf(time));

		setSubtitleSource();

		if(mUseSubtitle == true)
			executeSubtitleThread();
		Log.e("filePath : ", path);
	}

	public void setSubtitleSource()
	{
		String subtitlePath = path.substring(0, path.lastIndexOf(".")) + ".smi";
		File subtitleFile = new File(subtitlePath);

		if(subtitleFile.isFile() && subtitleFile.canRead())
		{
			mUseSubtitle = true;
			mSmiview = (TextView)findViewById(R.id.tv_smi);
			parsedSubtitleDataList = new ArrayList<SubtitleData>();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(subtitleFile.toString())), "MS949"));
				String s;
				long time = -1;
				String text = null;
				boolean startSubtitle = false;

				while((s = in.readLine()) != null)
				{
					if(s.contains("<SYNC"))
					{
						startSubtitle = true;
						if(time != -1) {
							parsedSubtitleDataList.add(new SubtitleData(time, text));
						}

						time = Integer.parseInt(s.substring(s.indexOf("=")+1, s.indexOf(">")));
						text = s.substring(s.indexOf(">")+1, s.length());
						text = text.substring(text.indexOf(">")+1, text.length());
					}
					else
					{
						if(startSubtitle == true)
							text = text + s;
					}
				}

				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			mUseSubtitle = false;
	}

	public void executeSubtitleThread()
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(mUseSubtitle) {
					try {
						Thread.sleep(200);
						subtitleHandler.sendMessage(subtitleHandler.obtainMessage());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				parsedSubtitleDataList.clear();
				indexSubtitle = 0;
			}
		}).start();
	}

	Handler subtitleHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if(currentTime > 0)
			{
				try {
					indexSubtitle = getIndexSubtitle(currentTime);

					if(indexSubtitle == -1)
						mSmiview.setText("");
					else
						mSmiview.setText(Html.fromHtml(parsedSubtitleDataList.get(indexSubtitle).getText()));

				} catch(Exception e) {}
			}
		}
	};

	public int getIndexSubtitle(long currentTime)
	{
		int l = 0;
		int m;
		int h = parsedSubtitleDataList.size();

		while(l <= h)
		{
			m = (l + h) / 2;
			if(parsedSubtitleDataList.get(m).getTime() <= currentTime && currentTime < parsedSubtitleDataList.get(m+1).getTime())
				return m;
			if(currentTime > parsedSubtitleDataList.get(m+1).getTime())
				l = m + 1;
			else
				h = m - 1;
		}

		return -1;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if(holdCheck==true){
			DisplayMetrics screen = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(screen);

			float x_changed = event.getRawX() - mTouchX;
			float y_changed = event.getRawY() - mTouchY;

			float coef = Math.abs (y_changed / x_changed);
			float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);

			if(event.getAction() == MotionEvent.ACTION_MOVE && onPPL == false)
			{
				mMove = true;

				if(coef > 3)
				{
					mSeekControlView.setVisibility(View.GONE);
					if(mTouchX < (getDeviceWidth() / 2))
					{
						Log.e("Brightness", "Brightness");
						doBrightnessTouch(y_changed);
						//this.mVolumeBrightnessControlView.setVisibility(View.VISIBLE);
						mControllerHandler = new Handler(){
							@Override
							public void handleMessage(Message msg) {
								mVolumeBrightnessControlView.setVisibility(View.GONE);
							}
						};
						this.mVolumeBrightnessControlView.setVisibility(View.VISIBLE);
						//mControllerHandler.sendEmptyMessageDelayed(0, 4000);
					}
					if(mTouchX > (getDeviceWidth() / 2))
					{
						Log.e("Volume", "Volume");
						doVolumeTouch(y_changed);
						//this.mVolumeBrightnessControlView.setVisibility(View.VISIBLE);
						//this.mVolumeBrightnessValue.setText(""+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
						mControllerHandler = new Handler(){
							@Override
							public void handleMessage(Message msg) {
								mVolumeBrightnessControlView.setVisibility(View.GONE);
							}
						};
						this.mVolumeBrightnessValue.setText(""+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
						this.mVolumeBrightnessControlView.setVisibility(View.VISIBLE);
						//mControllerHandler.sendEmptyMessageDelayed(0, 4000);
					}

					return true;
				}else if(coef < 3 || mSeekControlView.getVisibility() == View.VISIBLE)
				{
					if(xgesturesize < 0.02 && xgesturesize > -0.02)
					{
						mMove = false;
					}else if(coef < 0.5 && Math.abs(xgesturesize) > 1)
					{
						mVolumeBrightnessControlView.setVisibility(View.GONE);

						Log.e("SeekBartest", "                                               seekbar");
						mSeekControlHandler = new Handler(){
							@Override
							public void handleMessage(Message msg) {
								mSeekControlView.setVisibility(View.GONE);
							}
						};
						this.mSeekControlSmallValue.setText("[ "+((currentTimeS>seekValue)?"-":"+")+parseTime(Math.abs(currentTimeS-seekValue))+" ]");
						this.mSeekControlValue.setText(parseTime(currentTimeS));
						this.mSeekControlView.setVisibility(View.VISIBLE);
						//mSeekControlHandler.sendEmptyMessageDelayed(0, 2000);

						doSeekTouch(coef, xgesturesize, false);        

					}
				}

				return true;
			}
			else if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				mTouchX = event.getRawX();
				mTouchY = event.getRawY();

				mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				Log.e("GestureSize Up", ""+xgesturesize);

				if(mSeekControlView.getVisibility()==View.VISIBLE){
					mSeekControlHandler.sendEmptyMessageDelayed(0, 1000);

				}else if(mVolumeBrightnessControlView.getVisibility()==View.VISIBLE){
					mControllerHandler.sendEmptyMessageDelayed(0, 1000);
				}else if(mControlsView.getVisibility() == View.VISIBLE){
					mControllerHandler.sendEmptyMessageDelayed(0, 4000);
				}

				if(mMove==true && mSeek==true)
				{
					mMove = false;
					mSeek = false;

					Log.e("SeekValue : ", String.valueOf(seekValue));

					mMpegPlayer.seek(String.valueOf(seekValue));

					return true;
				}

				if(mMove == true)
				{
					mMove = false;
					return true;
				}

				if(mHold == false)
				{
					if(mPPLLayout.getVisibility() == View.GONE)///////////////////////////////////////////////////////////수정요망
					{
						if(mTouchPressed == false)
						{
							mTouchPressed = true;

							mControllerHandler = new Handler(){
								@Override
								public void handleMessage(Message msg) {
									mTitleBar.setVisibility(View.GONE);
									mControlsView.setVisibility(View.GONE);
									mPPLButton.setVisibility(View.GONE);
									if(mUseSubtitle) {
										RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
										params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
										params.addRule(RelativeLayout.CENTER_HORIZONTAL);
										params.setMargins(20, 20, 20, 20);

										mSmiview.setLayoutParams(params);
									}
									mTouchPressed = false;
								}
							};
							this.mTitleBar.setVisibility(View.VISIBLE);
							this.mControlsView.setVisibility(View.VISIBLE);
							this.mPPLButton.setVisibility(View.VISIBLE);
							if(mUseSubtitle) {
								RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
								params.addRule(RelativeLayout.ABOVE, mControlsView.getId());
								params.addRule(RelativeLayout.CENTER_HORIZONTAL);
								params.setMargins(20, 20, 20, 20);

								mSmiview.setLayoutParams(params);
							}
							//mControllerHandler.sendEmptyMessageDelayed(0, 10000);
							//displaySystemMenu(false);

						}
						else
						{
							mTouchPressed = false;
							mControllerHandler.removeMessages(0);
							this.mTitleBar.setVisibility(View.GONE);
							this.mControlsView.setVisibility(View.GONE);
							this.mPPLButton.setVisibility(View.GONE);
							if(mUseSubtitle) {
								RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
								params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
								params.addRule(RelativeLayout.CENTER_HORIZONTAL);
								params.setMargins(20, 20, 20, 20);

								mSmiview.setLayoutParams(params);
								//displaySystemMenu(false);
							}
						}
					}
					else
					{
						if(mPlay) {
							mMpegPlayer.resume();
							mTouchPressed = false;
							getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
							this.mTitleBar.setVisibility(View.GONE);
							this.mControlsView.setVisibility(View.GONE);
							this.mPPLButton.setVisibility(View.GONE);
						}

						//mPPLLayout.setVisibility(View.GONE);
						mSeekBar.setEnabled(true);
						onPPL = false;
					}
				}
				// hold 상태일 때,
				else
				{
					mUnHoldButtonView.setVisibility(View.VISIBLE);
				}

				return true;
			}

			return true;
		}else if(holdCheck == false){
			holdVideo();
			return true;
		}
		return true;
	}

	@Override
	public void onClick(View v)
	{
		if(onPPL == false) {
			switch (v.getId())
			{
			case R.id.hold_video:
				if(mPlay)
				{
					holdVideo();	
				}
				break;
			case R.id.unhold_button:
				unholdVideo();
				break;
			case R.id.play_pause:
				resumePause();
				break;
			case R.id.next_video:
				nextVideo();
				break;
			case R.id.prev_video:
				prevVideo();
				break;
				//case R.id.ratio_video:
				//        changeRatio();
				//        break;
			case R.id.btn_ppl:
				if(mPlay) 
					mMpegPlayer.pause();

				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////PPL json parser

				JSONParserHelper.parsingPPL();
				//imageView.
				LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
				for (int i = 1; i < 3; i++) {


					ImageView imageView = new ImageView(this);
					URL url;
					try {
						Log.e("ImageParser", ""+JSONParserHelper.product_image);
						url = new URL(JSONParserHelper.product_image);
						Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						imageView.setImageBitmap(bmp);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("ImageParser", "Fuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuck");
					}

					//imageView.setImageResource();
					LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT
							);
					layout.addView(imageView, p);
				}








				this.mTitleBar.setVisibility(View.INVISIBLE);
				this.mControlsView.setVisibility(View.INVISIBLE);

				drawer.animateOpen();
				//this.mPPLButton.setVisibility(View.GONE);
				//mPPLLayout.setVisibility(View.VISIBLE);
				//onPPL = true;
				//mSeekBar.setEnabled(false);
				break;
			case R.id.btn_slide:
				drawer.animateClose();
				this.mTitleBar.setVisibility(View.VISIBLE);
				this.mControlsView.setVisibility(View.VISIBLE);
				break;
			default:
				throw new RuntimeException();
			}
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
			/////////////////////////////////////////////////////////////////////////////////////////////////SeekBar

		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		String value = String.valueOf(seekBar.getProgress());
		Log.e("seekbar value : ", value);
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
		currentTimeS = (int)(currentTimeUs / 1000 / 1000);
		int videoDurationS = (int)(videoDurationUs / 1000 / 1000);

		currentTime = currentTimeUs / 1000;

		if(currentTimeS >= 0){
			mSeekBar.setMax(videoDurationS);
			mSeekBar.setProgress(currentTimeS);

			mCurrentTime.setText(parseTime(currentTimeS));
			mTotalTime.setText(parseTime(videoDurationS));

		}

		//Log.e("Seek Motion", "currentTimeS                  "+currentTimeS);

		if (isFinished) {
			isFinish = true;
			nextVideo();
			isFinish = false;
		}
	}

	@Override
	public void onFFResume(NotPlayingException result)
	{
		this.mPlayPauseButton.setImageResource(R.drawable.pause);
		this.mPlayPauseButton.setEnabled(true);

		//displaySystemMenu(false);
	}

	@Override
	public void onFFPause(NotPlayingException err)
	{
		this.mPlayPauseButton.setImageResource(R.drawable.play);
		this.mPlayPauseButton.setEnabled(true);
	}

	@Override
	public void onFFStop()
	{
	}

	@Override
	public void onFFSeeked(NotPlayingException result)
	{
		//if (result != null)
		//        throw new RuntimeException(result);
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
			.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}

	public void resumePause()
	{
		this.mPlayPauseButton.setEnabled(false);

		if (mPlay)
		{
			mMpegPlayer.pause();
			//displaySystemMenu(false);
		}
		else
		{
			mMpegPlayer.resume();
			//displaySystemMenu(true);
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

		if (min < 10 && min > -10)
			minS = "0" + min;
		else
			minS = min + "";
		if (sec < 10 && sec > -10)
			secS = "0" + sec;
		else
			secS = sec + "";

		if(hour > 0){
			String result = hour + " : " + minS + " : " + secS;
			return result;
		}else{
			String result = minS + " : " + secS;
			return result;
		}


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

	private int getDeviceHeight() {
		if (12 < Build.VERSION.SDK_INT) {
			Point p = new Point();
			getWindowManager().getDefaultDisplay().getSize(p);
			return p.y;
		} else {
			return getWindowManager().getDefaultDisplay().getHeight();
		}
	}

	// 홀드 처리
	public void holdVideo() {
		mHold = true;
		holdCheck = false;

		mHoldHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				mUnHoldButtonView.setVisibility(View.GONE);
			}
		};
		mUnHoldButtonView.setVisibility(View.VISIBLE);
		mHoldHandler.sendEmptyMessageDelayed(0, 4000);

		this.mTitleBar.setVisibility(View.GONE);
		this.mControlsView.setVisibility(View.GONE);
		this.mPPLButton.setVisibility(View.GONE);
	}

	public void unholdVideo() {
		mHold = false;
		holdCheck = true;
		mUnHoldButtonView.setVisibility(View.GONE);

		this.mTitleBar.setVisibility(View.VISIBLE);
		this.mControlsView.setVisibility(View.VISIBLE);
		this.mPPLButton.setVisibility(View.VISIBLE);
		//displaySystemMenu(true);
	}

	public void nextVideo() {
		if(videoList != null && index < videoList.size() - 1) {
			saveVideoTime();
			file = new File(videoList.get(++index));
			path = file.getAbsolutePath();
			setDataSource();
			mMpegPlayer.resume();
		}
	}

	public void prevVideo() {
		if(index > 0) {
			saveVideoTime();
			file = new File(videoList.get(--index));
			path = file.getAbsolutePath();
			setDataSource();
			mMpegPlayer.resume();
		}
	}

	public void saveVideoTime() {
		int now = (int) (mMpegPlayer.getCurrentTime() / 1000 / 1000);
		if(isFinish)
			dbAdapter.saveVideoTime(path, 1);
		else
			dbAdapter.saveVideoTime(path, now);

	}


	private void doBrightnessTouch(float y_changed)
	{
		float delta = -y_changed / getDeviceHeight() * 0.07f;
		WindowManager.LayoutParams lp = getWindow().getAttributes();

		if(brightnessCheck == false){
			SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
			lp.screenBrightness = pref.getFloat("brightness", 0.01f);
			lp.screenBrightness = (lp.screenBrightness-1)/14;
			getWindow().setAttributes(lp);
			brightnessCheck = true;
		}

		lp.screenBrightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);

		brightnessValue = (lp.screenBrightness*14)+1;
		this.mVolumeBrightnessValue.setText(""+(int)brightnessValue);
		getWindow().setAttributes(lp);
	}

	private void doVolumeTouch(float y_changed)
	{
		int delta = -(int) ((y_changed / getDeviceHeight()) * mAudioMax);
		int vol = (int) Math.min(Math.max(mVolume + delta, 0), mAudioMax);

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		AudioManager mAudioManager = 
				(AudioManager)getSystemService(AUDIO_SERVICE);
		if(holdCheck==true){
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP :
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, 
						AudioManager.FLAG_SHOW_UI);
				mControllerHandler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						mVolumeBrightnessControlView.setVisibility(View.GONE);
					}
				};
				this.mVolumeBrightnessValue.setText(""+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
				this.mVolumeBrightnessControlView.setVisibility(View.VISIBLE);
				mControllerHandler.sendEmptyMessageDelayed(0, 4000);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
						AudioManager.ADJUST_LOWER, 
						AudioManager.FLAG_SHOW_UI);
				mControllerHandler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						mVolumeBrightnessControlView.setVisibility(View.GONE);
					}
				};
				this.mVolumeBrightnessValue.setText(""+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
				this.mVolumeBrightnessControlView.setVisibility(View.VISIBLE);
				mControllerHandler.sendEmptyMessageDelayed(0, 4000);
				return true;
			case KeyEvent.KEYCODE_BACK:
				if (onPPL) {
					if(mPlay) {
						mMpegPlayer.resume();
						mTouchPressed = false;
						getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						this.mTitleBar.setVisibility(View.GONE);
						this.mControlsView.setVisibility(View.GONE);
						this.mPPLButton.setVisibility(View.GONE);
					}

					//mPPLLayout.setVisibility(View.GONE);
					mSeekBar.setEnabled(true);
					onPPL = false;
				} 
				else if (mHold);
				else {
					mUseSubtitle = false;
					saveVideoTime();
					SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = pref.edit();
					editor.putFloat("brightness", this.brightnessValue);
					editor.commit();
					finish();
				}
				return true;
			}

			return false;
		}else if(holdCheck==false){
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP :
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				return true;
			case KeyEvent.KEYCODE_BACK:
				return true;
			}
			return false;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////
	public void onUserLeaveHint(){
		Log.e("HOMEKEY!!!!!!!!!!", "HOMEKEY");
		if (onPPL) {
			if(mPlay) {
				mMpegPlayer.resume();
				mTouchPressed = false;
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				this.mTitleBar.setVisibility(View.GONE);
				this.mControlsView.setVisibility(View.GONE);
				this.mPPLButton.setVisibility(View.GONE);
			}

			mPPLLayout.setVisibility(View.GONE);
			mSeekBar.setEnabled(true);
			onPPL = false;
		} 
		else if (mHold);
		else {
			mUseSubtitle = false;
			saveVideoTime();
			SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putFloat("brightness", this.brightnessValue);
			editor.commit();
			finish();
		}
	}
	///////////////////////////////////////////////////////////////////////

	private void doSeekTouch(float coef, float xgesturesize, boolean seek)
	{

		if(coef > 0.5 || Math.abs(xgesturesize) < 1)
			return;

		long mCurrentTimeUs = mMpegPlayer.getCurrentTime();
		long mVideoDurationUs = mMpegPlayer.getVideoDuration();
		long value;

		int jump = (int) (Math.signum(xgesturesize) * ((600000 * Math.pow((xgesturesize), 4)) + 3000));

		if((jump > 0) && ((mCurrentTimeUs + jump) > mVideoDurationUs))
			jump = (int) (mVideoDurationUs - mCurrentTimeUs);
		if((jump < 0) && ((mCurrentTimeUs + jump) < 0))
			jump = (int) -mCurrentTimeUs;

		value = mCurrentTimeUs + jump;

		seekValue = (int)(value / 1000 / 1000);

		mSeek = true;
	}

	private void changeRatio()
	{
		if(mCurrentSize < SURFACE_BEST_FIT)
			mCurrentSize = mCurrentSize + 1;
		else
			mCurrentSize = SURFACE_BEST_FIT;

		switch(mCurrentSize)
		{
		case SURFACE_BEST_FIT:
			mMpegPlayer.changeRatioNative(SURFACE_BEST_FIT);
			break;
		case SURFACE_4_3:
			mMpegPlayer.changeRatioNative(SURFACE_4_3);
			break;
		case SURFACE_16_9:
			mMpegPlayer.changeRatioNative(SURFACE_16_9);
			break;
		}
	}

	private void cancelAsyncTask()
	{        
		if(FFmpegPlayer.stopTask != null)
			FFmpegPlayer.stopTask.cancel(true);
		if(FFmpegPlayer.setDataSourceTask != null)
			FFmpegPlayer.setDataSourceTask.cancel(true);
		if(FFmpegPlayer.seekTask != null)
			FFmpegPlayer.seekTask.cancel(true);
		if(FFmpegPlayer.pauseTask != null)
			FFmpegPlayer.pauseTask.cancel(true);
		if(FFmpegPlayer.resumeTask != null)
			FFmpegPlayer.resumeTask.cancel(true);
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		if(visibility != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION){
			Toast.makeText(getApplicationContext(), "a;slkdjfalksj", Toast.LENGTH_SHORT).show();
			this.mTitleBar.setVisibility(View.GONE);
			this.mControlsView.setVisibility(View.GONE);
			this.mPPLButton.setVisibility(View.GONE);

			if(mUseSubtitle){
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ABOVE, mControlsView.getId());
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				params.setMargins(20, 20, 20, 20);

				mSmiview.setLayoutParams(params);
			}
		}

	}
	/*mVideoView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		if(visibility != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION){
			Toast.makeText(getApplicationContext(), "a;slkdjfalksj", Toast.LENGTH_SHORT).show();



		}
	}
});*/

	/*	this.mTitleBar.setVisibility(View.VISIBLE);
		this.mControlsView.setVisibility(View.VISIBLE);
		this.mPPLButton.setVisibility(View.VISIBLE);
		if(mUseSubtitle) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ABOVE, mControlsView.getId());
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.setMargins(20, 20, 20, 20);

			mSmiview.setLayoutParams(params);
	 */


}