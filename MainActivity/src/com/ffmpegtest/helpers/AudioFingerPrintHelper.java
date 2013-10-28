package com.ffmpegtest.helpers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;

public class AudioFingerPrintHelper
{
	private String ffmpegPath;
	private String videoFilePath;
	
	// need parameter of video file path
	public AudioFingerPrintHelper(String ffmpegPath, String videoFilePath)
	{
		this.ffmpegPath = ffmpegPath;
		this.videoFilePath = videoFilePath;
	}
	
	// temp 삭제 확
	public ProcessRunnableHelper create()
	{
		final List<String> cmd = new LinkedList<String>();
		String path = Environment.getExternalStorageDirectory() + "/android/data/";
		File dataFile = new File(path, "temp");
		FFmpegCreateHelper.doChmod(dataFile, 777);
		cmd.add(ffmpegPath);
		cmd.add("-i");
		cmd.add(videoFilePath);
		cmd.add("-ac");
		cmd.add("1");
		cmd.add("-f");
		cmd.add("s16le");
		cmd.add("-ar");
		cmd.add("2000");
		cmd.add("-acodec");
		cmd.add("pcm_s16le");
		cmd.add(path + "/aaa.raw");
		//cmd.add(path + "/temp");
		
		final ProcessBuilder pb = new ProcessBuilder(cmd);
		
		return new ProcessRunnableHelper(pb);
	}
}
