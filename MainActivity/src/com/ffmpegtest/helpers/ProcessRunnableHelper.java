package com.ffmpegtest.helpers;

import java.io.IOException;

public class ProcessRunnableHelper implements Runnable
{
	private ProcessBuilder process;
	
	public ProcessRunnableHelper(ProcessBuilder process)
	{
		this.process = process;
	}
	
	@Override
	public void run()
	{
		Process proc = null;
		
		try {
			proc = process.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
