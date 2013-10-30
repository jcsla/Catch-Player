package com.ffmpegtest.helpers;

import java.util.Arrays;

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
}
