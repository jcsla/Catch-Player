package com.ffmpegtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;
import com.ffmpegtest.adapter.PPLPagerViewAdapter;
import com.ffmpegtest.adapter.VideoFileDBAdapter;
import com.ffmpegtest.helpers.AudioFingerPrintHelper;
import com.ffmpegtest.helpers.JSONHelper;
import com.ffmpegtest.helpers.Util;

public class VideoActivity extends Activity implements FFmpegListener, OnClickListener, OnSeekBarChangeListener, OnTouchListener
{
	public static FFmpegPlayer mMpegPlayer;
	//////////////////////////////////////////////////
	// UI Variable
	//////////////////////////////////////////////////
	private View mFullLayout;
	private View mVideoView;
	private View mTitleBar;
	private TextView mTitle;
	private View mControlsView;
	private SeekBar mSeekBar;
	private ImageButton mHoldButton;
	private ImageButton mPlayPauseButton;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private TextView mRealTime;

	private View mSeekVariationView;
	private TextView mSeekCurrentTimeValue;
	private TextView mSeekVariationValue;
	private View mVolumeBrightnessVariationView;
	private TextView mVolumeBrightnessValue;
	private ImageView mVolumeBrightnessImage;

	private TextView mSubtitleView;

	private View mUnHoldButtonView;
	private ImageButton mUnHoldButton;

	private ImageView mPPLButton;
	private ViewPager mPPLViewPager;
	private RelativeLayout mPPLLayout;
	private LinearLayout mPageMark;

	public static ProgressDialog progess;

	//////////////////////////////////////////////////
	// Value Variable
	//////////////////////////////////////////////////
	private VideoFileDBAdapter dbAdapter;
	private AudioManager mAudioManager;
	ArrayList<String> videoList;									// 비디오 리스트
	private int index;
	private File file;
	public static String path;
	ArrayList<SubtitleData> parsedSubtitleDataList;				// 자막
	private long currentTime;										// 현재시간 for 자막
	private int indexSubtitle;										// 자막 인덱스
	private int mAudioMax;											// Device의 Audio Max 값
	private float mVolume;
	private boolean mSeek = false;
	private int seekValue;
	private boolean mPlay = false;									// 플레이 체크 flag
	private boolean brightnessCheck = false;						// 밝기 체크 flag
	private float brightnessValue;
	private boolean mHold = false;									// 홀드 체크 flag
	private int currentTimeS;										// 현재시간 for PPL
	private boolean onPPL = false;
	private int mPPLPosition;										// PPL 선택 시의 현재 위치 변수
	private boolean mUseSubtitle = false;							// 자막 flag
	private boolean mTouchPressed = false;							// 터치 체크 flag
	private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
	private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
	private boolean isFinish = false;								// finish flag
	private float mTouchX;
	private float mTouchY;
	private Animation anim_in_bottom, anim_out_bottom, anim_in_top, anim_out_top;
	private Handler mSeekControlHandler;
	private Handler mVolumeBrightnessVariationHandler;
	private Handler mControllerHandler;
	private Handler mHoldHandler;
	private Util util = Util.getInstance();
	private boolean mMove = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DITHER);

		super.onCreate(savedInstanceState);

		getWindow().setBackgroundDrawable(null);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.video_surfaceview);

		mFullLayout = this.findViewById(R.id.full_layout);
		mFullLayout.setOnTouchListener(this);

		mVideoView = this.findViewById(R.id.video_view);

		mTitleBar = this.findViewById(R.id.title_bar);
		mTitle = (TextView) this.findViewById(R.id.title);

		mControlsView = this.findViewById(R.id.controls);
		mControlsView.setOnTouchListener(this);
		mSeekBar = (SeekBar) this.findViewById(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mCurrentTime = (TextView) this.findViewById(R.id.current_time);
		mTotalTime = (TextView) this.findViewById(R.id.total_time);
		mPlayPauseButton = (ImageButton) this.findViewById(R.id.play_pause);
		mPlayPauseButton.setOnClickListener(this);
		mHoldButton = (ImageButton) this.findViewById(R.id.hold_video);
		mHoldButton.setOnClickListener(this);
		mPPLButton = (ImageView) this.findViewById(R.id.ppl_button);
		mPPLButton.setOnClickListener(this);
		mRealTime = (TextView) this.findViewById(R.id.time);
		
		mUnHoldButtonView = this.findViewById(R.id.unhold_area);
		mUnHoldButton = (ImageButton) this.findViewById(R.id.unhold_video);
		mUnHoldButton.setOnClickListener(this);

		mSeekVariationView = this.findViewById(R.id.seek_variation_view);
		mSeekCurrentTimeValue = (TextView)this.findViewById(R.id.current_time_value);
		mSeekVariationValue = (TextView)this.findViewById(R.id.seek_variation_value);
		mVolumeBrightnessVariationView = this.findViewById(R.id.volume_brightness_variation_view);
		mVolumeBrightnessValue = (TextView)this.findViewById(R.id.volume_brightness_value);
		mVolumeBrightnessImage = (ImageView)this.findViewById(R.id.volume_brightness_image);

		mPPLLayout = (RelativeLayout)this.findViewById(R.id.ppl_view);
		mPageMark = (LinearLayout) this.findViewById(R.id.page_mark);
		mPPLViewPager = (ViewPager) this.findViewById(R.id.view_pager);
		mPPLViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override 
			public void onPageSelected(int position) {
				//이전 페이지에 해당하는 페이지 표시 이미지 변경
				mPageMark.getChildAt(mPPLPosition).setBackgroundResource(R.drawable.page_not);

				//현재 페이지에 해당하는 페이지 표시 이미지 변경    
				mPageMark.getChildAt(position).setBackgroundResource(R.drawable.page_select);
				mPPLPosition = position;                //이전 포지션 값을 현재로 변경
			}
			@Override public void onPageScrolled(int position, float positionOffest, int positionOffsetPixels) {}
			@Override public void onPageScrollStateChanged(int state) {}
		});

		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAlpha(160);
		mPPLLayout.setBackgroundColor(paint.getColor());
		if(android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		mControllerHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mTouchPressed = !mTouchPressed;
				hideControlsView();
			}
		};

		mVolumeBrightnessVariationHandler = new Handler() {
			public void handleMessage(Message msg) {
				mVolumeBrightnessVariationView.setVisibility(View.GONE);
			}
		};

		mSeekControlHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				mSeekVariationView.setVisibility(View.GONE);
			}
		};


		anim_in_bottom  = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
		anim_out_bottom = AnimationUtils.loadAnimation(this, R.anim.slide_to_bottom);
		anim_in_top = AnimationUtils.loadAnimation(this, R.anim.slide_from_top);
		anim_out_top = AnimationUtils.loadAnimation(this, R.anim.slide_to_top);
		doBrightnessTouch(1f);
		dbAdapter = new VideoFileDBAdapter(this);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		mMpegPlayer = null;
		mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
		mMpegPlayer.setMpegListener(this);
		progess = util.getProgress(this);

		setDataSource();
		
		final Handler mTimeChangeHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mRealTime.setText(getCurrentTime());
			}
		};
		
		new Thread() { // 현재 시간을 출력해주는 Thread
			public void run() {
				while(!isFinish) {
					mTimeChangeHandler.sendEmptyMessage(0);
					try {
						Thread.sleep(1000 * 60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
		
		mMpegPlayer.resume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mPlay = false;
		mMpegPlayer.pause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mMpegPlayer.setMpegListener(null);
		mMpegPlayer.stop();
	}

	private void setDataSource()
	{
		HashMap<String, String> params = new HashMap<String, String>();

		// set font for ass
		File assFont = new File(Environment.getExternalStorageDirectory(), "DroidSansFallback.ttf");
		params.put("ass_default_font_path", assFont.getAbsolutePath());

		if(videoList == null)
		{
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

		mPlayPauseButton.setImageResource(R.drawable.selector_pause);
		mPlayPauseButton.setEnabled(true);

		mPlay = true;
		mHold = false;

		mMpegPlayer.setDataSource(path, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo, mSubtitleStreamNo);

		int time = dbAdapter.getVideoTime(path);
		if(time > 0)
			mMpegPlayer.seek(String.valueOf(time));

		setSubtitleSource();

		if(mUseSubtitle == true)
			executeSubtitleThread();

		String dramaName = dbAdapter.getVideoFingerPrint(path);
		if(dramaName != null && dramaName.equals(""))
			new AudioFingerPrintHelper(MainActivity.mFFmpegInstallPath, path).fingerTask.execute(this);
		else
			JSONHelper.dramaName = dramaName;

		mControllerHandler.removeMessages(0);
		mControllerHandler.sendEmptyMessageDelayed(0, 4000);
	}

	public void setSubtitleSource()
	{
		String subtitlePath = path.substring(0, path.lastIndexOf(".")) + ".smi";
		File subtitleFile = new File(subtitlePath);

		if(subtitleFile.isFile() && subtitleFile.canRead())
		{
			mUseSubtitle = true;
			mSubtitleView = (TextView)findViewById(R.id.subtitle_view);
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
						if(time != -1)
							parsedSubtitleDataList.add(new SubtitleData(time, text));

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
			}
			catch (IOException e) {
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
			public void run()
			{
				while(mUseSubtitle)
				{
					try {
						Thread.sleep(200);
						subtitleHandler.sendMessage(subtitleHandler.obtainMessage());
					}
					catch (InterruptedException e) {
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
						mSubtitleView.setText("");
					else
						mSubtitleView.setText(Html.fromHtml(parsedSubtitleDataList.get(indexSubtitle).getText()));

				}
				catch(Exception e) {

				}
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
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(mHold == false) {
			switch (keyCode) 
			{
			case KeyEvent.KEYCODE_VOLUME_UP:
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
						AudioManager.ADJUST_RAISE,
						AudioManager.FLAG_SHOW_UI);
				doVolumeUpDown();
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
						AudioManager.ADJUST_LOWER, 
						AudioManager.FLAG_SHOW_UI);
				doVolumeUpDown();
				break;
			case KeyEvent.KEYCODE_BACK:
				if(onPPL == true)
					closePPLView();
				else {
					saveVideoTime();
					saveBrightness();
					isFinish = true;
					finish();
				}

				break;
			}
		}
		return true;
	}

	public void closePPLView()
	{
		onPPL = false;
		mTouchPressed = false;

		mPPLLayout.setVisibility(View.GONE);
		mMpegPlayer.resume();
	}

	@Override
	public void onClick(View v)
	{
		mControllerHandler.removeMessages(0);
		switch (v.getId())
		{
		case R.id.play_pause:
			resumePause();
			break;
		case R.id.next_video:
			nextVideo();
			break;
		case R.id.prev_video:
			prevVideo();
			break;
		case R.id.hold_video:
			holdVideo();
			break;
		case R.id.unhold_video:
			unholdVideo();
			break;
		case R.id.ppl_button:
			viewPPL();
			break;
		}
	}

	public void resumePause()
	{
		this.mPlayPauseButton.setEnabled(false);

		if (mPlay)
			mMpegPlayer.pause();
		else
			mMpegPlayer.resume();

		mPlay = !mPlay;
	}

	public void holdVideo()
	{
		mHold = true;

		mHoldHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mUnHoldButtonView.setVisibility(View.GONE);
			}
		};
		mUnHoldButtonView.setVisibility(View.VISIBLE);
		mHoldHandler.sendEmptyMessageDelayed(0, 4000);

		hideControlsView();
	}

	public void hideControlsView() {
		mControllerHandler.removeMessages(0);
		mControlsView.startAnimation(anim_out_bottom);
		mControlsView.setVisibility(View.GONE);

		mTitleBar.startAnimation(anim_out_top);
		mTitleBar.setVisibility(View.GONE);
	}

	public void unholdVideo()
	{
		mHold = false;
		mTouchPressed = false;
		mUnHoldButtonView.setVisibility(View.GONE);
	}
	////////////////////////////////////////////////////////////////////////
	public void nextVideo()
	{
		if (videoList != null && index < videoList.size() - 1) 
		{
			saveVideoTime();
			file = new File(videoList.get(++index));
			path = file.getAbsolutePath();
			setDataSource();
			mMpegPlayer.resume();
		}
	}

	public void prevVideo()
	{
		if (index > 0) 
		{
			saveVideoTime();
			file = new File(videoList.get(--index));
			path = file.getAbsolutePath();
			setDataSource();
			mMpegPlayer.resume();
		}
	}

	public void viewPPL()
	{
		if(mPlay) 
			mMpegPlayer.pause();

		mPPLViewPager.setAdapter(new PPLPagerViewAdapter(this, currentTimeS, mPageMark));
		mPPLPosition = 0;

		hideControlsView();

		onPPL = true;
		mPPLLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if(mHold == false) { //홀드가 걸려있지 않으면
			DisplayMetrics screen = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(screen);

			float x_changed = event.getRawX() - mTouchX;
			float y_changed = event.getRawY() - mTouchY;

			float coef = Math.abs (y_changed / x_changed);
			float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);


			switch(event.getAction())
			{
			case MotionEvent.ACTION_UP:
				doActionUP();
				break;
			case MotionEvent.ACTION_DOWN:
				doActionDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				doActionMove(coef, x_changed, y_changed, xgesturesize);
				break;
			}
		} 

		else {
			mHoldHandler.removeMessages(0);
			mUnHoldButtonView.setVisibility(View.VISIBLE);
			mHoldHandler.sendEmptyMessageDelayed(0, 4000);
		}
		return true;
	}

	public void doActionMove(float coef, float x_changed, float y_changed, float xgesturesize) {
		mMove = true;
		mControllerHandler.removeMessages(0);
		if(coef > 3)
		{
			mSeekVariationView.setVisibility(View.GONE);
			if(mTouchX < (getDeviceWidth() / 2)) {
				mVolumeBrightnessVariationHandler.removeMessages(0);
				mVolumeBrightnessVariationView.setVisibility(View.VISIBLE);
				mVolumeBrightnessVariationHandler.sendEmptyMessageDelayed(0, 2000);
				doBrightnessTouch(y_changed);
			}

			if(mTouchX > (getDeviceWidth() / 2)) {
				doVolumeTouch(y_changed);
			}
		} 

		else if(coef < 3 || mSeekVariationView.getVisibility() == View.VISIBLE)
		{
			if(xgesturesize < 0.02 && xgesturesize > -0.02)
				mMove = false;
			else if(coef < 0.5 && Math.abs(xgesturesize) > 1)
			{
				mVolumeBrightnessVariationView.setVisibility(View.GONE);

				Log.e("SeekBartest", "Value is " + xgesturesize);

				mSeekVariationValue.setText("[ "+((currentTimeS>seekValue)?"-":"+")+parseTime(Math.abs(currentTimeS-seekValue))+" ]");
				mSeekCurrentTimeValue.setText(parseTime(currentTimeS));
				mSeekVariationView.setVisibility(View.VISIBLE);
				mSeekControlHandler.sendEmptyMessageDelayed(0, 2000);

				doSeekTouch(coef, xgesturesize, false);        
			}
		}
	}

	public void doActionDown(MotionEvent event) 
	{
		mTouchX = event.getRawX();
		mTouchY = event.getRawY();

		mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	public void doActionUP()
	{
		if(mMove == false) {
			mTouchPressed = !mTouchPressed;
			mControllerHandler.removeMessages(0);
			Log.e("mTouchPressed = " + mTouchPressed, "mMove = " + mMove);

			if(mTouchPressed == true && mMove == false)
			{
				mTitleBar.startAnimation(anim_in_top);
				mTitleBar.setVisibility(View.VISIBLE);

				mControlsView.startAnimation(anim_in_bottom);
				mControlsView.setVisibility(View.VISIBLE);

				mControllerHandler.sendEmptyMessageDelayed(0, 4000);
			} else {
				hideControlsView();
			}
		}
		else {
			mMove = false;
			mControllerHandler.sendEmptyMessageDelayed(0, 4000);
		}

		if(mSeek==true)
		{
			mSeek = false;

			mMpegPlayer.seek(String.valueOf(seekValue));
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
	
	public String getCurrentTime() {
	    String time = "";
	    Calendar cal = Calendar.getInstance();
	    time = String.format("%02d : %02d",
	            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
	         
	    return time;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		mControllerHandler.removeMessages(0);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		String value = String.valueOf(seekBar.getProgress());
		mMpegPlayer.seek(value);
		mControllerHandler.sendEmptyMessageDelayed(0, 4000);
	}

	@Override
	public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams)
	{
		if (err != null)
		{
			String format = getResources().getString(R.string.main_could_not_open_stream);
			String message = String.format(format, err.getMessage());

			Builder builder = new AlertDialog.Builder(VideoActivity.this);
			builder.setTitle(R.string.app_name).setMessage(message).setOnCancelListener(
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

		if(currentTimeS > 0) {
			mSeekBar.setMax(videoDurationS);
			mSeekBar.setProgress(currentTimeS);

			mCurrentTime.setText(parseTime(currentTimeS));
			mTotalTime.setText(parseTime(videoDurationS));
		}

		if (isFinished) {
			nextVideo();
			isFinish = false;
		}
	}

	@Override
	public void onFFResume(NotPlayingException result)
	{
		this.mPlayPauseButton.setImageResource(R.drawable.selector_pause);
		this.mPlayPauseButton.setEnabled(true);
	}

	@Override
	public void onFFPause(NotPlayingException err)
	{
		this.mPlayPauseButton.setImageResource(R.drawable.selector_play);
		this.mPlayPauseButton.setEnabled(true);
	}

	@Override
	public void onFFStop()
	{
	}

	@Override
	public void onFFSeeked(NotPlayingException result)
	{
	}

	/**
	 * 작성자 : 이준영
	 * 메소드 이름 : parseTime
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

		if(hour > 0)
		{
			String result = hour + " : " + minS + " : " + secS;
			return result;
		}
		else
		{
			String result = minS + " : " + secS;
			return result;
		}
	}

	private int getDeviceWidth()
	{
		if (12 < Build.VERSION.SDK_INT)
		{
			Point p = new Point();
			getWindowManager().getDefaultDisplay().getSize(p);
			return p.x;
		}
		else
		{
			return getWindowManager().getDefaultDisplay().getWidth();
		}
	}

	private int getDeviceHeight()
	{
		if (12 < Build.VERSION.SDK_INT)
		{
			Point p = new Point();
			getWindowManager().getDefaultDisplay().getSize(p);
			return p.y;
		}
		else
		{
			return getWindowManager().getDefaultDisplay().getHeight();
		}
	}

	public void saveVideoTime() {
		int now = (int) (mMpegPlayer.getCurrentTime() / 1000 / 1000);
		if(isFinish)
			dbAdapter.saveVideoTime(path, 1, JSONHelper.dramaName);
		else
			dbAdapter.saveVideoTime(path, now, JSONHelper.dramaName);
	}

	public void saveBrightness() {
		SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putFloat("brightness", this.brightnessValue);
		editor.commit();
	}

	private void doBrightnessTouch(float y_changed)
	{
		float delta = -y_changed / getDeviceHeight() * 0.07f;
		WindowManager.LayoutParams lp = getWindow().getAttributes();

		if(brightnessCheck == false){
			SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
			lp.screenBrightness = pref.getFloat("brightness", 1f);
			lp.screenBrightness = (lp.screenBrightness-1)/14;
			getWindow().setAttributes(lp);
			brightnessCheck = true;
		}

		lp.screenBrightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);

		brightnessValue = (lp.screenBrightness*14)+1;
		mVolumeBrightnessValue.setText(""+(int)brightnessValue);
		mVolumeBrightnessImage.setBackgroundResource(R.drawable.bright);
		getWindow().setAttributes(lp);
	}

	private void doVolumeTouch(float y_changed)
	{
		int delta = -(int) ((y_changed / getDeviceHeight()) * mAudioMax);
		int vol = (int) Math.min(Math.max(mVolume + delta, 0), mAudioMax);

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
		doVolumeUpDown();
	}

	private void doVolumeUpDown() {
		mVolumeBrightnessVariationHandler.removeMessages(0);
		mVolumeBrightnessValue.setText(""+mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		mVolumeBrightnessImage.setBackgroundResource(R.drawable.sound);
		mVolumeBrightnessVariationView.setVisibility(View.VISIBLE);
		mVolumeBrightnessVariationHandler.sendEmptyMessageDelayed(0, 2000);
	}

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
}