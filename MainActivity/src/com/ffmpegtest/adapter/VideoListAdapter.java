package com.ffmpegtest.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.ffmpegtest.R;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter {

	private static final int KB = 1024;
	private static final int MB = KB * KB;
	private static final int GB = MB * KB;

	private LayoutInflater inflater = null;
	private HashMap<String, ArrayList<String>> video = null;
	private ArrayList<String> keySet = null;
	private ViewHolder viewHolder = null;
	private Context mContext = null;
	private String currentPath = null;
	private String root = null;
	private boolean inRoot = false;

	public VideoListAdapter(Context c , HashMap<String, ArrayList<String>> video, String currentPath){
		this.mContext = c;
		this.inflater = LayoutInflater.from(c);
		this.video = video;
		this.currentPath = currentPath;
		root = Environment.getExternalStorageDirectory().getPath();
		keySet = new ArrayList<String>(video.keySet());
	}
	
	public VideoListAdapter(Context c, ArrayList<String> video) {
		this.keySet = video;
		this.inflater = LayoutInflater.from(c);
		this.mContext = c;
	}
	
	public VideoListAdapter(Context c , HashMap<String, ArrayList<String>> video, String currentPath, boolean inRoot){
		this(c, video, currentPath);
		this.inRoot = inRoot;
	}

	// Adapter가 관리할 List의 개수를 설정 합니다.
	@Override
	public int getCount() {
		if(video == null)
			return keySet.size();
		else if(!currentPath.equals(root) || inRoot) 
			return video.get(currentPath).size();
		else
			return keySet.size();
	}

	// Adapter가 관리하는 List의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public String getItem(int position) {
		if(video == null)
			return keySet.get(position);
		else if(!currentPath.equals(root) || inRoot) 
			return currentPath + '/' + video.get(currentPath).get(position);
		else
			return keySet.get(position);
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

		if(v == null){
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.video_list, null);
			viewHolder.tv_title = (TextView)v.findViewById(R.id.tv_video_title);
			viewHolder.tv_size = (TextView)v.findViewById(R.id.tv_video_size);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}

		String[] split = getItem(position).split("/");
		String fileName = split[split.length - 1];
		viewHolder.tv_title.setText(fileName);
		
		File f = new File(getItem(position));
		if(f.isDirectory())
			viewHolder.tv_size.setText(video.get(f.getAbsolutePath()).size() + " 비디오");
		else {
			String display_size;
			double size = f.length();
			
			if (size > GB)
				display_size = String.format("%.2f GB ", (double)size / GB);
			else if (size < GB && size > MB)
				display_size = String.format("%.2f MB ", (double)size / MB);
			else if (size < MB && size > KB)
				display_size = String.format("%.2f KB ", (double)size/ KB);
			else
				display_size = String.format("%.2f Bytes ", (double)size);
			
			viewHolder.tv_size.setText(display_size);
		}

		return v;
	}

	public ArrayList<String> getArrayList(){
		return video.get(currentPath);
	}

	/*
	 * ViewHolder 
	 * getView의 속도 향상을 위해 쓴다.
	 * 한번의 findViewByID 로 재사용 하기 위해 viewHolder를 사용 한다.
	 */
	class ViewHolder{
		public TextView tv_title = null;
		public TextView tv_size = null;
	}

	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}

	private void free(){
		inflater = null;
		video = null;
		viewHolder = null;
		mContext = null;
	}

}
