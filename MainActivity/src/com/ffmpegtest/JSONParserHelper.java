package com.ffmpegtest;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
				Log.e("JSONParser", ""+jObs.toString());
				PPLData ppl = new PPLData();
				
				ppl.store_link = jObs.getString("store_link");
				ppl.product_image = jObs.getString("product_image");
				ppl.price = jObs.getInt("price");
				ppl.product_code = jObs.getInt("product_code");
				ppl.drama_code = jObs.getString("drama_code");
				ppl.brand_name = jObs.getString("brand_name");
				ppl.product_name = jObs.getString("product_name");
				
				//Log.e("JSONParser", ""+ppl.store_link);
				
				pplData.add(ppl);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("JSONParser", "JSONParser Exception!!!!");
		}
	}
}
