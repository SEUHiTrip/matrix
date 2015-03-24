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

public class AppController {
	private static String TAG = "AppController";
	private static String type = "App";
	private static String rootUrl = Confg.ip + type + "/";

	public static enum app_name {
		game_minecraft, game_car, office_word, office_ppt, office_excel, internet, skype, pic, video, file, cam, drone
	};

	RequestQueue mQueue;

	public AppController(RequestQueue queue) {
		mQueue = queue;
	}

	public void open(int screen, app_name an, ErrorListener errorListener,
			Listener<JSONObject> listener) throws JSONException {

		String url = rootUrl + "open?app_name=" + an + "&screen=" + screen;

		Log.d(TAG, "req : " + url);

		mQueue.add(new JsonObjectRequest(Method.POST, url, null, listener,
				errorListener));
	}

	public void close(int screen, ErrorListener errorListener,
			Listener<JSONObject> listener) throws JSONException {
		String url = rootUrl + "close?screen=" + screen;

		Log.d(TAG, "req : " + url);

		mQueue.add(new JsonObjectRequest(Method.POST, url, null, listener,
				errorListener));
	}

}
