package seu.lab.matrix.controllers;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class AppController {
	private static String TAG = "AppController";
	private static String type = "App";

	public static enum app_name {
		game_minecraft, game_car, office_word, office_ppt, office_excel, internet, skype, pic, video, file, cam, drone
	};

	RequestQueue mQueue;

	public AppController(RequestQueue queue) {
		mQueue = queue;
	}

	public void open(int screen, app_name an, ErrorListener errorListener,
			Listener<JSONObject> listener) throws JSONException {

		String url = Confg.WEB_API+type+"/" + "open?app_name=" + an + "&screen=" + screen;

		Log.d(TAG, "req : " + url);

		int socketTimeout = 30000;//30 seconds - change to what you want
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		
		JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, null, listener,
				errorListener);
		request.setRetryPolicy(policy);

		
		mQueue.add(request);
	}

	public void close(int screen, ErrorListener errorListener,
			Listener<JSONObject> listener) throws JSONException {
		String url = Confg.WEB_API+type+"/" + "close?screen=" + screen;

		Log.d(TAG, "req : " + url);

		mQueue.add(new JsonObjectRequest(Method.POST, url, null, listener,
				errorListener));
	}

}
