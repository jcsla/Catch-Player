package com.ffmpegtest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParserHelper {
	
	public static String product_image;
	
	public static void parsingPPL(String dName, int dTime){

		JSONParser jParser = new JSONParser();
		JSONArray json = jParser.getJSONFromUrl("http://kdspykim2.cafe24.com:8080/get_ppl_data?drama_code="+dName+"&current_time="+dTime);
		Log.e("JSONParser", ""+json.toString());
		try {
			JSONObject jObs = json.getJSONObject(0);

			String store_link = jObs.getString("store_link");
			product_image = jObs.getString("product_image");
			int price = jObs.getInt("price");
			int product_code = jObs.getInt("product_code");
			String drama_code = jObs.getString("drama_code");
			String brand_name = jObs.getString("brand_name");
			String product_name = jObs.getString("product_name");

		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("JSONParser", "JSONParser Exception!!!!");
		}
	}
}
