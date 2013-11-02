package com.ffmpegtest.helpers;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Environment;

import com.ffmpegtest.*;
import com.appunite.ffmpeg.FFmpegPlayer;

public class AudioFingerPrintHelper
{
	private String ffmpegPath;
	private String videoFilePath;
	
	private static String fp;
	
	private static int bufferSize;
	
	public AudioFingerPrintHelper(String ffmpegPath, String videoFilePath)
	{
		this.ffmpegPath = ffmpegPath;
		this.videoFilePath = videoFilePath;
	}
	
	public static void startAudioFingerPrint()
	{
		final AudioFingerPrintHelper fingerprint = new AudioFingerPrintHelper(MainActivity.mFFmpegInstallPath, VideoActivity.path);
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg) {
				fingerprint.create().run();
				float data[] = readAudioDataFile();
				fp = VideoActivity.mMpegPlayer.codegen(data, bufferSize);
				System.out.println(fp);
				return null;
			}
			
			// json request & response
			// android json parser needs asynctask class...
			@Override
			protected void onPostExecute(Void result) {
				JSONHelper.postAFPServer(fp);
			}

		}.execute();
	}
	
	public ProcessRunnableHelper create()
	{
		final List<String> cmd = new LinkedList<String>();
		String path = Environment.getExternalStorageDirectory() + "/android/data/";
		File dataFile = new File(path, "audioData");
		if(dataFile.exists())
			dataFile.delete();
		
		FFmpegCreateHelper.doChmod(dataFile, 777);
		cmd.add(ffmpegPath);
		cmd.add("-i");
		cmd.add(videoFilePath);
		cmd.add("-ac");
		cmd.add("1");
		cmd.add("-ar");
		cmd.add("2000");
		cmd.add("-f");
		cmd.add("s16le");
		cmd.add("-t");
		cmd.add("120");
		cmd.add("-ss");
		cmd.add("0");
		cmd.add(path + "/audioData");
		
		final ProcessBuilder pb = new ProcessBuilder(cmd);
		
		return new ProcessRunnableHelper(pb);
	}
	
	public static float[] readAudioDataFile()
	{
		File file = new File(Environment.getExternalStorageDirectory() + "/android/data/audioData");
		InputStream in = null;

		if (file.isFile())
		{
			long size = file.length();
			try {
				in = new FileInputStream(file);
				return readStreamAsFloatArray(in, size);
			} catch (Exception e) {

			}
		}
		
		return null;
	}
	
	public static float[] readStreamAsFloatArray(InputStream in, long size)
	{
		bufferSize = (int) (size);
		float[] result = new float[bufferSize];
		DataInputStream is = new DataInputStream(in);
		try
		{
			int i = 0;
			while(is.available() > 0) {
				result[i] = (float) (swap(is.readShort()) / 32768.0);
				i = i + 1;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static short swap(short x)
	{
		return (short)((x << 8) | ((x >> 8) & 0xff));
    }
}
