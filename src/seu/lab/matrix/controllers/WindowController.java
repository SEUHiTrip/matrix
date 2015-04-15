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

public class WindowController {
	private static String TAG="AppController";
	private static String type="Window";
	private RequestQueue mQueue;
	
	public WindowController(RequestQueue queue){
		this.mQueue = queue;
	}
	
	public void setMouse(int screen) throws JSONException {		
		String url=Confg.WEB_API+type+"/"+"setMouse?screen="+screen;
		
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
	
	public void setFocus(int screen) throws JSONException {		
		String url=Confg.WEB_API+type+"/"+"setFocus?screen="+screen;
		
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
	
	public void click() throws JSONException {		
		String url=Confg.WEB_API+"mouse/click";
		
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

		mQueue.add(new JsonObjectRequest(Method.GET,url, null,listener,errorListener));
	}
	
	public void press() throws JSONException {		
		String url=Confg.WEB_API+"mouse/press";
		
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

		mQueue.add(new JsonObjectRequest(Method.GET,url, null,listener,errorListener));
	}
	
	public void release() throws JSONException {		
		String url=Confg.WEB_API+"mouse/release";
		
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

		mQueue.add(new JsonObjectRequest(Method.GET,url, null,listener,errorListener));
	}
}
