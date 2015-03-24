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
import com.google.gson.Gson;

public class FilesController {
	private static String TAG="FilesController";
	private static String type="Files";
	private static String rootUrl=Confg.ip+type+"/";
	private static Gson gson = new Gson();
	
	public class Files{
		public String type;
		public String name;
		public String location;
		//public Thumbnail thumbnail;
		public long size;
		public String file_type;
	}

	private RequestQueue mQueue;
	
	public FilesController(RequestQueue queue){
		this.mQueue = queue;
	}
	
	public void create(String parent,String name,String file_type) throws JSONException {		
		JSONObject jo=new JSONObject();
		jo.put("parent", parent);
		jo.put("name", name);
		jo.put("file_type", file_type);
		
		String url=rootUrl+"create";
		
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
				try {
					//temporarily write for test
					String type=res.getString("type");
					Log.d("onresponse", type);
					Files file=gson.fromJson(res.getJSONObject("new_file").toString(), Files.class);
					//this works even when gsonResObject contain more members than the local Files.class
					Log.d("onresponse",file.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, jo ,listener,errorListener));
	}

	public void copy(String ori,String dest) throws JSONException {
		
		
		JSONObject jo=new JSONObject();
		jo.put("ori", ori);
		jo.put("dest", dest);
		
		String url=rootUrl+"copy";
		
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
				try {
					//temporarily write for test
					String result=res.getString("result");
					Log.d("onresponse", result);
					Files file=gson.fromJson(res.getJSONObject("new_file").toString(), Files.class);
					//this works even when gsonResObject contain more members than the local Files.class
					Log.d("onresponse",file.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, jo ,listener,errorListener));
	}

	public void cut(String ori,String dest) throws JSONException {
		
		
		JSONObject jo=new JSONObject();
		jo.put("ori", ori);
		jo.put("dest", dest);
		
		String url=rootUrl+"cut";
		
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
				try {
					//temporarily write for test
					String result=res.getString("result");
					Log.d("onresponse", result);
					Files file=gson.fromJson(res.getJSONObject("new_file").toString(), Files.class);
					//this works even when gsonResObject contain more members than the local Files.class
					Log.d("onresponse",file.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, jo ,listener,errorListener));
	}
	
	public void open(int screen,String file_name) throws JSONException {
		
		
		String url=rootUrl+"open?screen="+screen+"&file_name="+file_name;
		
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

		mQueue.add(new JsonObjectRequest(Method.POST,url, null ,listener,errorListener));
	}
	
	public void close(int screen,String file_name) throws JSONException {
		
		
		String url=rootUrl+"close?screen="+screen+"&file_name="+file_name;
		
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

		mQueue.add(new JsonObjectRequest(Method.POST,url, null ,listener,errorListener));
	}

}
