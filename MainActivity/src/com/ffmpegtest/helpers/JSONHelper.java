package com.ffmpegtest.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.ffmpegtest.VideoActivity;

import android.os.AsyncTask;

public class JSONHelper
{
	
	public static String dramaName = "";
	private static final String AFPServerURL = "http://211.110.33.122/query";
	private static final String codever = "4.12";
	
	public static void postAFPServer(final String fp)
	{
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg) {
				postData2AFPServer(fp);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				VideoActivity.progess.dismiss();
			}

		}.execute();
	}
	
	public static void postData2AFPServer(String fp)
	{
		InputStream inputStream = null;
		try {
			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(AFPServerURL);

			String json = "";

			// 3. build jsonObject
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("fp", fp);
			jsonObject.accumulate("codever", codever);

			// 4. convert JSONObject to JSON to String
			json = jsonObject.toString();

			// ** Alternative way to convert Person object to JSON string usin
			// Jackson Lib
			// ObjectMapper mapper = new ObjectMapper();
			// json = mapper.writeValueAsString(person);

			// 5. set json to StringEntity
			StringEntity se = new StringEntity(json);

			// 6. set httpPost Entity
			httpPost.setEntity(se);

			// 7. Set some headers to inform server about the type of the
			// content
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			convertInputStreamToString(inputStream);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void convertInputStreamToString(InputStream inputStream)
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		
		try {
			
			while ((line = bufferedReader.readLine()) != null)
			{
				//JSONObject jsonObject = new JSONObject(line);
				//System.out.println(new String(jsonObject.getString("program_name")));
				//System.out.println(new String(jsonObject.getString("program_entry")));
				//System.out.println(new String(jsonObject.getString("track_id")));
				System.out.println(line);
				JSONObject jsonObject = new JSONObject(line);
				System.out.println(new String(jsonObject.getString("program_name")));
				System.out.println(new String(jsonObject.getString("program_entry")));
				
				dramaName = new String(jsonObject.getString("key"));
			}

			inputStream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}