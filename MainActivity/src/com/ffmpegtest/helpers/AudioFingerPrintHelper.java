package com.ffmpegtest.helpers;

import java.util.LinkedList;
import java.util.List;

public class AudioFingerPrintHelper
{
	private String ffmpegPath;
	
	// need parameter of video file path
	public AudioFingerPrintHelper(String ffmpegPath)
	{
		this.ffmpegPath = ffmpegPath;
	}
	
	public ProcessRunnableHelper create()
	{
		final List<String> cmd = new LinkedList<String>();
		
		cmd.add(ffmpegPath);
		cmd.add("-i");
		//..... add other command
		
		final ProcessBuilder pb = new ProcessBuilder(cmd);
		
		return new ProcessRunnableHelper(pb);
	}
}
