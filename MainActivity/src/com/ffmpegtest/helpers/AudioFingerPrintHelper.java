package com.ffmpegtest.helpers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;

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
		cmd.add("-f");
		cmd.add("s16le");
		cmd.add("-ar");
		cmd.add("2000");
		cmd.add("-t");
		cmd.add("0");
		cmd.add("-ss");
		cmd.add("120");
		cmd.add("-acodec");
		cmd.add("pcm_s16le");
		cmd.add(path + "/audioData");
		
		final ProcessBuilder pb = new ProcessBuilder(cmd);
		
		return new ProcessRunnableHelper(pb);
	}
}
