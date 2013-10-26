package com.ffmpegtest.adapter;

import java.io.File;
import java.util.ArrayList;
import com.ffmpegtest.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FolderListAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private ArrayList<String> fileList = null;
	private ArrayList<Integer> videoLength = null;
	private ViewHolder viewHolder = null;
	private Context mContext = null;

	public FolderListAdapter(Context c , ArrayList<String> fileList , ArrayList<Integer> videoLength){
		this.mContext = c;
		this.inflater = LayoutInflater.from(c);
		this.fileList = fileList;
		this.videoLength = videoLength;
	}

	// Adapter가 관리할 List의 개수를 설정 합니다.
	@Override
	public int getCount() {
		return fileList.size();
	}

	// Adapter가 관리하는 List의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public String getItem(int position) {
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

		if(v == null){
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.video_list, null);
			viewHolder.tv_title = (TextView)v.findViewById(R.id.tv_video_title);
			viewHolder.tv_size = (TextView)v.findViewById(R.id.tv_video_size);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}


		File file = new File(getItem(position));
		String fileName = file.getName();
		viewHolder.tv_title.setText(fileName);
		
		if(file.isDirectory()) 
			viewHolder.tv_size.setText(videoLength.get(position) + " 비디오");

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
