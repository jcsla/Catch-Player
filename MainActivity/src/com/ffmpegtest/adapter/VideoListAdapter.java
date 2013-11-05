package com.ffmpegtest.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.ffmpegtest.R;
import com.ffmpegtest.helpers.ImageLoader;
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
	private ImageLoader imageLoader = null;
	
	public VideoListAdapter(Context c , ArrayList<File> fileList){
		this.mContext = c;
		this.inflater = LayoutInflater.from(c);
		this.fileList = fileList;
		this.util = Util.getInstance();
		this.imageLoader = new ImageLoader(c);
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

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}

		String fileName = util.removeExtension(file.getName());
		viewHolder.tv_title.setText(fileName);
		String path = file.getAbsolutePath();
		imageLoader.DisplayImage(path, viewHolder.iv_video);
		
		double size = file.length();
		if(file.canRead() && size > 0) {
			String display_size = util.getVideoSize(size);
			viewHolder.tv_size.setText(display_size);
		}

		return v;
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
