package com.ffmpegtest.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

public class FFmpegCreateHelper
{
	private static final int IO_BUFFER_SIZE = 32256;

	public static void installBinaryFromRaw(Context context, int resId, File file)
	{
		final InputStream rawStream = context.getResources().openRawResource(resId);
		final OutputStream binStream = getFileOutputStream(file);

		if (rawStream != null && binStream != null)
		{
			pipeStreams(rawStream, binStream);

			try {
				rawStream.close();
				binStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			// doChmod(file, CHMOD_EXEC_VALUE);
		}
	}

	public static OutputStream getFileOutputStream(File file)
	{
		try {
			return new FileOutputStream(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void pipeStreams(InputStream is, OutputStream os)
	{
		byte[] buffer = new byte[IO_BUFFER_SIZE];
		int count;
		
		try {
			while ((count = is.read(buffer)) > 0)
				os.write(buffer, 0, count);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
