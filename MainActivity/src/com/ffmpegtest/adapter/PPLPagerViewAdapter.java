package com.ffmpegtest.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ffmpegtest.JSONParserHelper;
import com.ffmpegtest.PPLData;
import com.ffmpegtest.R;
import com.ffmpegtest.helpers.JSONHelper;

public class PPLPagerViewAdapter extends PagerAdapter
	{
		private LayoutInflater mInflater;
		private ArrayList<PPLData> mPPLList;
		private Context c;
		private LinearLayout mPageMark;

		public PPLPagerViewAdapter(Context c, int currentTimeS, LinearLayout mPageMark)
		{
			super();
			this.c = c;
			mInflater = LayoutInflater.from(c);
			mPPLList = new ArrayList<PPLData>();
			this.mPageMark = mPageMark;
			JSONParserHelper.pplData.clear();
			JSONParserHelper.parsingPPL(JSONHelper.dramaName, currentTimeS);
			if(JSONParserHelper.pplData.size()==0){
				PPLData ppl = new PPLData();
				Bitmap bitmap = null;
				try {
					bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.parser);
					bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
					ppl.setProduct_image(bitmap);
				} catch(Exception e){

				}

				ppl.setPrice(0);
				ppl.setProduct_code(0);
				ppl.setDrama_code("");
				ppl.setBrand_name("현재 시청장면에");
				ppl.setProduct_name("등록된 상품이 없습니다.");

				JSONParserHelper.pplData.add(ppl);
			}
			//imageView.			
			for (int i = 0; i < JSONParserHelper.pplData.size(); i++) {
				try {
					PPLData ppl = JSONParserHelper.pplData.get(i);
					Log.e("Product", ppl.getProduct_name());
					mPPLList.add(ppl);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			initPageMark();
		}

		@Override
		public int getCount()
		{
			return mPPLList.size();
		}

		@Override
		public Object instantiateItem(View pager, int position)
		{
			View v = mInflater.inflate(R.layout.vp_ppl, null);
			ImageView iv_ppl = (ImageView)v.findViewById(R.id.iv_ppl_image);
			iv_ppl.setImageBitmap(mPPLList.get(position).getProduct_image());
			TextView tv_ppl_title = (TextView)v.findViewById(R.id.tv_ppl_title);
			tv_ppl_title.setText(mPPLList.get(position).getProduct_name());
			TextView tv_ppl_brand = (TextView)v.findViewById(R.id.tv_ppl_brand);
			tv_ppl_brand.setText(mPPLList.get(position).getBrand_name());
			TextView tv_ppl_price = (TextView)v.findViewById(R.id.tv_ppl_price);
			tv_ppl_price.setText(mPPLList.get(position).getPrice() + "원");
			if(mPPLList.get(position).getPrice()==0){
				tv_ppl_price.setText("");
			}

			((ViewPager)pager).addView(v, 0);

			return v; 
		}

		@Override
		public void destroyItem(View pager, int position, Object view)
		{    
			((ViewPager)pager).removeView((View)view);
		}

		@Override
		public boolean isViewFromObject(View pager, Object obj)
		{
			return pager == obj; 
		}

		private void initPageMark()
		{
			mPageMark.removeAllViews();
			for(int i=0; i<getCount(); i++)
			{
				ImageView iv = new ImageView(c);	//페이지 표시 이미지 뷰 생성
				iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

				//첫 페이지 표시 이미지 이면 선택된 이미지로
				if(i==0)
					iv.setBackgroundResource(R.drawable.page_select);
				else	//나머지는 선택안된 이미지로
					iv.setBackgroundResource(R.drawable.page_not);

				//LinearLayout에 추가
				mPageMark.addView(iv);
			}
		}

		@Override public void restoreState(Parcelable arg0, ClassLoader arg1) {}
		@Override public Parcelable saveState() { return null; }
		@Override public void startUpdate(View arg0) {}
		@Override public void finishUpdate(View arg0) {}
	}