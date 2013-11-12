package com.ffmpegtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.ffmpegtest.helpers.Util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

public class OptionActivity extends Activity implements OnItemClickListener {

	private ListView list_view;
	private String[] str_option = { "PPL 안보기", "재생 설정" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		list_view = (ListView) findViewById(R.id.video_list);
		ArrayList<String> option = new ArrayList<String>(
				Arrays.asList(str_option));
		list_view.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, option));
		list_view.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		switch (position) {
		}
	}
}
