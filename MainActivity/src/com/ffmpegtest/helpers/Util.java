package com.ffmpegtest.helpers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android.app.ProgressDialog;
import android.content.Context;

public class Util {
	private Util() {}

	public static Util util = new Util();
	public static Util getInstance() {
		return util;
	}
	public static final String supportedVideoFileFormats[] = { "mp4", "wmv",
		"avi", "mkv", "dv", "rm", "mpg", "mpeg", "flv", "divx", "swf",
		"h264", "h263", "h261", "3gp", "3gpp", "asf", "mov", "m4v", "ogv",
		"vob", "vstream", "ts", "webm", "vro", "tts", "tod", "rmvb", "rec",
		"ps", "ogx", "ogm", "nuv", "nsv", "mxf", "mts", "mpv2", "mpeg1",
		"mpeg2", "mpeg4", "mpe", "mp4v", "mp2v", "mp2", "m2ts", "m2t",
		"m2v", "m1v", "amv", "3gp2" };

	public String removeExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	public String getVideoSize(double size) {
		final int KB = 1024;
		final int MB = KB * KB;
		final int GB = MB * KB;

		String display_size;

		if (size > GB)
			display_size = String.format("%.2f GB ", (double) size / GB);
		else if (size < GB && size > MB)
			display_size = String.format("%.2f MB ", (double) size / MB);
		else if (size < MB && size > KB)
			display_size = String.format("%.2f KB ", (double) size / KB);
		else
			display_size = String.format("%.2f Bytes ", (double) size);

		return display_size;
	}
	
	public void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}
	public boolean isVideoFile(String file) {
		if (file == null || file == "") {
			return false;
		}
		String ext = file.toString();
		String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
		if (Arrays.asList(supportedVideoFileFormats).contains(
				sub_ext.toLowerCase())) {
			return true;
		}
		return false;
	}

	public ProgressDialog getProgress(Context c) {
		ProgressDialog pDialog = new ProgressDialog(c);
		pDialog.setTitle("잠시만 기다려주세요.");
		pDialog.setMessage("데이터를 불러오는 중입니다.");
		pDialog.setCancelable(false);

		return pDialog;
	}
}
