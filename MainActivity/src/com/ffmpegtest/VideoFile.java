package com.ffmpegtest;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoFile implements Parcelable {
	private String path;
	private String name;
	private int time;
	private int new_video;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
	public VideoFile(String path, String name, int time, int new_video) {
		this.path = path;
		this.name = name;
		this.time = time;
		this.new_video = new_video;
	}
	
	public VideoFile(Parcel src) {
		this(src.readString(), src.readString(), src.readInt(), src.readInt());
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
		dest.writeString(name);
		dest.writeInt(time);
		dest.writeInt(new_video);
	}
	
	public int getNew_video() {
		return new_video;
	}
	public void setNew_video(int new_video) {
		this.new_video = new_video;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public static final Parcelable.Creator<VideoFile> CREATOR = new Creator<VideoFile>() {

		@Override
		public VideoFile createFromParcel(Parcel source) {
			return new VideoFile(source);
		}

		@Override
		public VideoFile[] newArray(int size) {
			return new VideoFile[size];
		}
	};
}
