package com.ffmpegtest.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.ffmpegtest.R;
import com.ffmpegtest.helpers.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private ArrayList<File> fileList = null;
	private ViewHolder viewHolder = null;
	private Context mContext = null;
	private Util util = null;

	public VideoListAdapter(Context c , ArrayList<File> fileList){
		this.mContext = c;
		this.inflater = LayoutInflater.from(c);
		this.fileList = fileList;
		this.util = Util.getInstance();
	}

	// Adapter가 관리할 List의 개수를 설정 합니다.
	@Override
	public int getCount() {
		return fileList.size();
	}

	// Adapter가 관리하는 List의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public File getItem(int position) {
		return fileList.get(position);
	}

	// Adapter가 관리하는 List의 Item 의 position 값의 ID 를 얻어 옵니다.
	@Override
	public long getItemId(int position) {
		return position;
	}

	public String getVideoSize(double size) {
		final int KB = 1024;
		final int MB = KB * KB;
		final int GB = MB * KB;

		String display_size;

		if (size > GB)
			display_size = String.format("%.2f GB ", (double)size / GB);
		else if (size < GB && size > MB)
			display_size = String.format("%.2f MB ", (double)size / MB);
		else if (size < MB && size > KB)
			display_size = String.format("%.2f KB ", (double)size/ KB);
		else
			display_size = String.format("%.2f Bytes ", (double)size);

		return display_size;
	}

	// ListView의 뿌려질 한줄의 Row를 설정 합니다.
	@Override
	public View getView(int position, View convertview, ViewGroup parent) {

		View v = convertview;

		File file = getItem(position);

		if(v == null){
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.video_list, null);
			viewHolder.tv_title = (TextView)v.findViewById(R.id.tv_video_title);
			viewHolder.tv_size = (TextView)v.findViewById(R.id.tv_video_size);
			viewHolder.iv_video = (ImageView)v.findViewById(R.id.iv_folder);

			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
			if(thumbnail != null) {
				thumbnail = ResizeBitmap(thumbnail, 250, 150);

				viewHolder.iv_video.setImageBitmap(thumbnail);
			}

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}

		String fileName = util.removeExtension(file.getName());
		viewHolder.tv_title.setText(fileName);

		double size = file.length();
		if(file.canRead() && size > 0) {
			String display_size = getVideoSize(size);
			viewHolder.tv_size.setText(display_size);
		}

		return v;
	}

		public Bitmap ResizeBitmap(Bitmap bitmap, int width, int height) {
			Bitmap sizingBmp = Bitmap.createScaledBitmap(bitmap, width, height, true);
			
			return sizingBmp;
		}

	/*
	 * ViewHolder 
	 * getView의 속도 향상을 위해 쓴다.
	 * 한번의 findViewByID 로 재사용 하기 위해 viewHolder를 사용 한다.
	 */
	class ViewHolder{
		public TextView tv_title = null;
		public TextView tv_size = null;
		public ImageView iv_video = null;
	}

	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}

	private void free(){
		inflater = null;
		viewHolder = null;
		mContext = null;
	}

}
