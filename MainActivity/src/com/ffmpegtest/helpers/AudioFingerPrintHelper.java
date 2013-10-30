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
	
	public AudioFingerPrintHelper(String ffmpegPath, String videoFilePath)
	{
		this.ffmpegPath = ffmpegPath;
		this.videoFilePath = videoFilePath;
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
	
	public static void startAudioFingerPrint()
	{
		final AudioFingerPrintHelper fingerprint = new AudioFingerPrintHelper(MainActivity.mFFmpegInstallPath, VideoActivity.path);
		
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg) {
				//fingerprint.create().run();
				readAudioDataFile();
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				System.out.println("onPostExcute");
				
				// param.h main.cxx
				// 여기서 데이터 읽어서 코드젠으로 변환 후 json 형태로 서버에 전송!!!
				//String path = Environment.getExternalStorageDirectory() + "/android/data";
				//File listFile = new File(path);
				//for(File f : listFile.listFiles()) {
				//	String str = f.getName();
				//	System.out.println(str);
				//}
				//readAudioDataFile();
				//System.out.println(data[0]);
				//String s = mMpegPlayer.codegen(data, data.length);
				//System.out.println(s);
			}

		}.execute();
	}
	
	public static float[] readAudioDataFile()
	{
		File file = new File(Environment.getExternalStorageDirectory() + "/android/data/audioData");
		InputStream in = null;

		if (file.isFile())
		{
			long size = file.length();
			System.out.println(size);
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
		int bufferSize = (int) (size);
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
		String s = VideoActivity.mMpegPlayer.codegen(result, bufferSize);
		System.out.println(s);
		return result;
	}
	
	public static short swap(short x)
	{
		return (short)((x << 8) | ((x >> 8) & 0xff));
    }
}
