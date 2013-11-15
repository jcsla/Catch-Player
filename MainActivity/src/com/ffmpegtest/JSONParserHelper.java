package com.ffmpegtest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class JSONParserHelper {
	public static ArrayList<PPLData> pplData = new ArrayList<PPLData>();
		
	public static void parsingPPL(String dName, int dTime){

		JSONParser jParser = new JSONParser();
		JSONArray json = jParser.getJSONFromUrl("http://kdspykim2.cafe24.com:8080/get_ppl_data?drama_code="+dName+"&current_time="+dTime);
		//Log.e("JSONParser", ""+json.toString());
		try {
			for(int i=0;i<json.length();i++){
				JSONObject jObs = json.getJSONObject(i);
				//Log.e("JSONParser", ""+jObs.toString());
				PPLData ppl = new PPLData();
				
				ppl.setStore_link(jObs.getString("store_link"));
				String img = jObs.getString("product_image");
				URL url;
				try {
					url = new URL(img);
					Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
					bmp = Bitmap.createScaledBitmap(bmp, 300, 300, true);
					ppl.setProduct_image(bmp);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ppl.setPrice(jObs.getInt("price"));
				ppl.setProduct_code(jObs.getInt("product_code"));
				ppl.setDrama_code(jObs.getString("drama_code"));
				ppl.setBrand_name(jObs.getString("brand_name"));
				ppl.setProduct_name(jObs.getString("product_name"));
				
				//Log.e("JSONParser", ""+ppl.store_link);
				
				pplData.add(ppl);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("JSONParser", "JSONParser Exception!!!!");
		}
	}
}
