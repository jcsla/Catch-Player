/*
 * FFmpegPlayer.java
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

package com.appunite.ffmpeg;

import java.util.Arrays;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.view.Surface;

public class FFmpegPlayer {
	public static StopTask stopTask;
	public static SetDataSourceTask setDataSourceTask;
	public static SeekTask seekTask;
	public static PauseTask pauseTask;
	public static ResumeTask resumeTask;
	
	public class StopTask extends AsyncTask<Void, Void, Void> {

		private final FFmpegPlayer player;

		public StopTask(FFmpegPlayer player) {
			this.player = player;
		}

		@Override
		protected Void doInBackground(Void... params) {
			System.out.println("stoptask execute!");
			player.stopNative();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (player.mpegListener != null)
				player.mpegListener.onFFStop();
			
			this.cancel(true);
			stopTask = null;
		}
		
		@Override
		protected void onCancelled() {
			stopTask = null;
			this.cancel(true);
			super.onCancelled();
		}
	}

	private static class SetDataSourceTaskResult {
		FFmpegError error;
		FFmpegStreamInfo[] streams;
	}

	public class SetDataSourceTask extends AsyncTask<Object, Void, SetDataSourceTaskResult>
	{
		private final FFmpegPlayer player;

		public SetDataSourceTask(FFmpegPlayer player)
		{
			this.player = player;
		}

		@Override
		protected SetDataSourceTaskResult doInBackground(Object... params)
		{
			System.out.println("setdatasoucetask execute!");
			String url = (String) params[0];
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) params[1];
			Integer videoStream = (Integer) params[2];
			Integer audioStream = (Integer) params[3];
			Integer subtitleStream = (Integer) params[4];

			int videoStreamNo = videoStream == null ? -1 : videoStream.intValue();
			int audioStreamNo = audioStream == null ? -1 : audioStream.intValue();
			int subtitleStreamNo = subtitleStream == null ? -1 : subtitleStream.intValue();

			int err = player.setDataSourceNative(url, map, videoStreamNo, audioStreamNo, subtitleStreamNo);
			SetDataSourceTaskResult result = new SetDataSourceTaskResult();
			if (err < 0) {
				result.error = new FFmpegError(err);
				result.streams = null;
			} else {
				result.error = null;
				result.streams = player.getStreamsInfo();
			}
			return result;
		}

		@Override
		protected void onPostExecute(SetDataSourceTaskResult result) {
			if (player.mpegListener != null)
				player.mpegListener.onFFDataSourceLoaded(result.error,
						result.streams);
			
			this.cancel(true);
			setDataSourceTask = null;
		}
		
		@Override
		protected void onCancelled() {
			setDataSourceTask = null;
			this.cancel(true);
			super.onCancelled();
		}
	}

	public class SeekTask extends
			AsyncTask<String, Void, NotPlayingException> {

		private final FFmpegPlayer player;

		public SeekTask(FFmpegPlayer player) {
			this.player = player;
		}

		@Override
		protected NotPlayingException doInBackground(String... params) {
			System.out.println("seektask execute!");;
			try {
				long value = Long.parseLong(params[0]) * 1000 * 1000;
				player.seekNative(value);
			} catch (NotPlayingException e) {
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(NotPlayingException result) {
			if (player.mpegListener != null)
				player.mpegListener.onFFSeeked(result);
			
			this.cancel(true);
			seekTask = null;
		}
		
		@Override
		protected void onCancelled() {
			seekTask = null;
			this.cancel(true);
			super.onCancelled();
		}
	}

	public class PauseTask extends
			AsyncTask<Void, Void, NotPlayingException> {

		private final FFmpegPlayer player;

		public PauseTask(FFmpegPlayer player) {
			this.player = player;
		}

		@Override
		protected NotPlayingException doInBackground(Void... params) {
			System.out.println("pausetask execute!");
			try {
				player.pauseNative();
				return null;
			} catch (NotPlayingException e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(NotPlayingException result) {
			if (player.mpegListener != null)
				player.mpegListener.onFFPause(result);
			
			this.cancel(true);
			pauseTask = null;
		}
		
		@Override
		protected void onCancelled() {
			pauseTask = null;
			this.cancel(true);
			super.onCancelled();
		}
	}

	public class ResumeTask extends
			AsyncTask<Void, Void, NotPlayingException> {

		private final FFmpegPlayer player;

		public ResumeTask(FFmpegPlayer player) {
			this.player = player;
		}

		@Override
		protected NotPlayingException doInBackground(Void... params) {
			System.out.println("resumetask execute!");
			try {
				player.resumeNative();
				return null;
			} catch (NotPlayingException e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(NotPlayingException result) {
			if (player.mpegListener != null)
				player.mpegListener.onFFResume(result);
			
			this.cancel(true);
			resumeTask = null;
		}
		
		@Override
		protected void onCancelled() {
			resumeTask = null;
			this.cancel(true);
			super.onCancelled();
		}
	}

	static {
		NativeTester nativeTester = new NativeTester();
		if (nativeTester.isNeon()) {
			System.out.println("neon");
			System.loadLibrary("ffmpeg-neon");
			System.loadLibrary("ffmpeg-jni-neon");
			System.loadLibrary("echoprint-jni");
		} else {
			System.out.println("default");
			System.loadLibrary("ffmpeg");
			System.loadLibrary("ffmpeg-jni");
			System.loadLibrary("echoprint-jni");
		}
	}

	public static final int UNKNOWN_STREAM = -1;
	public static final int NO_STREAM = -2;
	private FFmpegListener mpegListener = null;
	private final RenderedFrame mRenderedFrame = new RenderedFrame();

	private int mNativePlayer;
	private final Activity activity;

	private Runnable updateTimeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mpegListener != null) {
				//System.out.println(mCurrentTimeUs + " " + mVideoDurationUs);
				mpegListener.onFFUpdateTime(mCurrentTimeUs,
					mVideoDurationUs, mIsFinished);
			}
		}

	};

	private long mCurrentTimeUs;
	private long mVideoDurationUs;
	private FFmpegStreamInfo[] mStreamsInfos = null;
	private boolean mIsFinished = false;
	
	public long getCurrentTime()
	{
		return mCurrentTimeUs;
	}
	
	public long getVideoDuration()
	{
		return mVideoDurationUs;
	}

	static class RenderedFrame {
		public Bitmap bitmap;
		public int height;
		public int width;
	}

	public FFmpegPlayer(FFmpegDisplay videoView, Activity activity) {
		this.activity = activity;
		int error = initNative();
		if (error != 0)
			throw new RuntimeException(String.format(
					"Could not initialize player: %d", error));
		videoView.setMpegPlayer(this);
	}

	@Override
	protected void finalize() throws Throwable {
		deallocNative();
		super.finalize();
	}

	private native int initNative();

	private native void deallocNative();

	private native int setDataSourceNative(String url,
			Map<String, String> dictionary, int videoStreamNo,
			int audioStreamNo, int subtitleStreamNo);

	private native void stopNative();

	native void renderFrameStart();

	native void renderFrameStop();

	private native void seekNative(long positionUs) throws NotPlayingException;

	private native long getVideoDurationNative();

	public native void render(Surface surface);

	/**
	 * 
	 * @param streamsInfos
	 *            - could be null
	 */
	private void setStreamsInfo(FFmpegStreamInfo[] streamsInfos) {
		this.mStreamsInfos = streamsInfos;
	}

	/**
	 * Return streamsInfo
	 * 
	 * @return return streams info after successful setDataSource or null
	 */
	protected FFmpegStreamInfo[] getStreamsInfo() {
		return mStreamsInfos;
	}

	public void stop() {
		stopTask = new StopTask(this);
		stopTask.execute();
	}

	private native void pauseNative() throws NotPlayingException;

	private native void resumeNative() throws NotPlayingException;
	
	public native void changeRatioNative(int surfaceType);
	
	public native void closeStreamNative();
	
	public native String codegen(float data[], int numSamples);

	public void pause() {
		pauseTask = new PauseTask(this);
		pauseTask.execute();
	}

	public void seek(String positionUs) {
		seekTask = new SeekTask(this);
		seekTask.execute(positionUs);
	}

	public void resume() {
		resumeTask = new ResumeTask(this);
		resumeTask.execute();
	}

	private Bitmap prepareFrame(int width, int height) {
		// Bitmap bitmap =
		// Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		this.mRenderedFrame.height = height;
		this.mRenderedFrame.width = width;
		return bitmap;
	}

	private void onUpdateTime(long currentUs, long maxUs, boolean isFinished) {

		this.mCurrentTimeUs = currentUs;
		this.mVideoDurationUs = maxUs;
		this.mIsFinished  = isFinished;
		activity.runOnUiThread(updateTimeRunnable);
	}

	private AudioTrack prepareAudioTrack(int sampleRateInHz,
			int numberOfChannels) {

		for (;;) {
			int channelConfig;
			if (numberOfChannels == 1) {
				channelConfig = AudioFormat.CHANNEL_OUT_MONO;
			} else if (numberOfChannels == 2) {
				channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
			} else if (numberOfChannels == 3) {
				channelConfig = AudioFormat.CHANNEL_OUT_FRONT_CENTER
						| AudioFormat.CHANNEL_OUT_FRONT_RIGHT
						| AudioFormat.CHANNEL_OUT_FRONT_LEFT;
			} else if (numberOfChannels == 4) {
				channelConfig = AudioFormat.CHANNEL_OUT_QUAD;
			} else if (numberOfChannels == 5) {
				channelConfig = AudioFormat.CHANNEL_OUT_QUAD
						| AudioFormat.CHANNEL_OUT_LOW_FREQUENCY;
			} else if (numberOfChannels == 6) {
				channelConfig = AudioFormat.CHANNEL_OUT_5POINT1;
			} else if (numberOfChannels == 8) {
				channelConfig = AudioFormat.CHANNEL_OUT_7POINT1;
			} else {
				channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
			}
			try {
				int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
						channelConfig, AudioFormat.ENCODING_PCM_16BIT);
				AudioTrack audioTrack = new AudioTrack(
						AudioManager.STREAM_MUSIC, sampleRateInHz,
						channelConfig, AudioFormat.ENCODING_PCM_16BIT,
						minBufferSize, AudioTrack.MODE_STREAM);
				return audioTrack;
			} catch (IllegalArgumentException e) {
				if (numberOfChannels > 2) {
					numberOfChannels = 2;
				} else if (numberOfChannels > 1) {
					numberOfChannels = 1;
				} else {
					throw e;
				}
			}
		}
	}

	private void setVideoListener(FFmpegListener mpegListener) {
		this.setMpegListener(mpegListener);
	}

	public void setDataSource(String url) {
		setDataSource(url, null, UNKNOWN_STREAM, UNKNOWN_STREAM, NO_STREAM);
	}

	public void setDataSource(String url, Map<String, String> dictionary, int videoStream, int audioStream, int subtitlesStream)
	{
		setDataSourceTask = new SetDataSourceTask(this);
		setDataSourceTask.execute(url, dictionary, Integer.valueOf(videoStream), Integer.valueOf(audioStream), Integer.valueOf(subtitlesStream));
	}

	public FFmpegListener getMpegListener() {
		return mpegListener;
	}

	public void setMpegListener(FFmpegListener mpegListener)
	{
		this.mpegListener = mpegListener;
	}
	
	
	
	// not used
	private void getAudioData(byte[] audioData, int numSamples)
	{	
		float normalizingValue = Byte.MAX_VALUE;
		float normalizeAudioData[] = new float[numSamples];
		for (int i = 0; i < numSamples; i++) 
            normalizeAudioData[i] = audioData[i] / normalizingValue;
		
		System.out.println(audioData.length);
		
		//System.out.println(Arrays.toString(normalizeAudioData));
		//String code = this.codegen(normalizeAudioData, numSamples);
		//System.out.println(code.length());
	}
}