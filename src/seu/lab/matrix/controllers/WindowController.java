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
	private static String rootUrl=Confg.WEB_API+type+"/";
	private RequestQueue mQueue;
	
	public WindowController(RequestQueue queue){
		this.mQueue = queue;
	}
	
	public void setMouse(int screen) throws JSONException {		
		String url=rootUrl+"setMouse?screen="+screen;
		
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
		String url=rootUrl+"setFocus?screen="+screen;
		
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
