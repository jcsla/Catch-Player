package com.ffmpegtest;

public class SubtitleData
{
	private long time;
	private String text;
	
	public SubtitleData(long time, String text)
	{
		setTime(time);
		setText(text);
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}
	
	public long getTime()
	{
		return time;
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
	
	public String getText()
	{
		return text;
	}
}
