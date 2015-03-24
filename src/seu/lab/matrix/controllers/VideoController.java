package seu.lab.matrix.controllers;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class VideoController {
	private static String TAG="FolderController";
	private static String type="Video";
	private static String rootUrl=Confg.WEB_API+type+"/";
	
	RequestQueue mQueue;
	
	public VideoController(RequestQueue queue) {
		mQueue = queue;
	}
	
	public void play(int screen,String video_name, ErrorListener errorListener, Listener<JSONObject> listener) throws JSONException {
		
		String url=rootUrl+"play?screen="+screen+"&video_name="+video_name;
		
		Log.d(TAG, "req : " + url);

		mQueue.add(new JsonObjectRequest(Method.POST,url, null,listener,errorListener));
	}
	
	public void continue_pause(int screen) throws JSONException {
		
		//video_name="c:\\Users\\qf\\Desktop\\LynnTemp\\bigHero.mkv";
		
		String url=rootUrl+"continue_pause?screen="+screen;
		
		Log.d(TAG, "req : " + url);

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "onError : " + error.toString());
			}
		};

		Listener<JSONObject> listener = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				Log.d(TAG, "onResponse : " + res.toString());
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, null,listener,errorListener));
	}

	public void close(int screen) throws JSONException {		
		String url=rootUrl+"close?screen="+screen;
		
		Log.d(TAG, "req : " + url);

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "onError : " + error.toString());
			}
		};

		Listener<JSONObject> listener = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				Log.d(TAG, "onResponse : " + res.toString());
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, null,listener,errorListener));
	}
	
	public void forward(int screen) throws JSONException {		
		String url=rootUrl+"forward?screen="+screen;
		
		Log.d(TAG, "req : " + url);

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "onError : " + error.toString());
			}
		};

		Listener<JSONObject> listener = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				Log.d(TAG, "onResponse : " + res.toString());
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, null,listener,errorListener));
	}
	
	public void backward(int screen) throws JSONException {

		String url=rootUrl+"backward?screen="+screen;
		
		Log.d(TAG, "req : " + url);

		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "onError : " + error.toString());
			}
		};

		Listener<JSONObject> listener = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				Log.d(TAG, "onResponse : " + res.toString());
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, null,listener,errorListener));
	}
	
	
}
