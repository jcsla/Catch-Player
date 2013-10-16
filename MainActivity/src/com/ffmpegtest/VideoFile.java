package com.ffmpegtest;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoFile implements Parcelable {
	private String path;
	private String name;
	private int time;
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
	
	public VideoFile(String path, String name, int time) {
		this.path = path;
		this.name = name;
		this.time = time;
	}
	
	public VideoFile(String path, String name) {
		this(path, name, 0);
	}
	
	public VideoFile(Parcel src) {
		this(src.readString(), src.readString(), src.readInt());
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
