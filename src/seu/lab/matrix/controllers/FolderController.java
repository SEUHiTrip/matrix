package seu.lab.matrix.controllers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import seu.lab.matrix.controllers.FilesController.Files;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

public class FolderController {
	private static String TAG="FolderController";
	private static String type="Folder";
	private static String rootUrl=Confg.WEB_API+type+"/";
	private static Gson gson = new Gson();

	
	public class Folder{
		public String type;
		public String name;
		public String location;
		//public Thumbnail thumbnail;
		public int contain_num;
	}

	private RequestQueue mQueue;
	
	public FolderController(RequestQueue queue) {
		this.mQueue = queue;
	}
	
	public void get() throws JSONException {
		
		
		String url=rootUrl+"get";
		
		Log.d(TAG, "req : " + url);
		
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "onError : " + error.toString());
			}
		};

		Listener<JSONArray> listener = new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray res) {
				try {
					JSONObject j;
					String t;
					for (int i = 0; i < res.length(); i++) {
						j=(JSONObject) res.get(i);
						t=j.getString("type");
						if (t.equals("Files")) {
							Files file=gson.fromJson(res.get(i).toString(), Files.class);
							Log.d("res",file.toString());

						}else if (t.equals("Folder")) {
							Folder folder=gson.fromJson(res.get(i).toString(), Folder.class);
							Log.d("res",folder.toString());

						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		mQueue.add(new JsonArrayRequest(Method.GET,url, null ,listener,errorListener));
	}

	public void get(String folderPath) throws JSONException {
		
		
		String url=rootUrl+"get?pwd="+folderPath;
		
		Log.d(TAG, "req : " + url);
		
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "onError : " + error.toString());
			}
		};

		Listener<JSONArray> listener = new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray res) {
				try {
					JSONObject j;
					String t;
					for (int i = 0; i < res.length(); i++) {
						j=(JSONObject) res.get(i);
						t=j.getString("type");
						if (t.equals("Files")) {
							Files file=gson.fromJson(res.get(i).toString(), Files.class);
							Log.d("res",file.toString());

						}else if (t.equals("Folder")) {
							Folder folder=gson.fromJson(res.get(i).toString(), Folder.class);
							Log.d("res",folder.toString());

						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		mQueue.add(new JsonArrayRequest(Method.GET,url, null ,listener,errorListener));
	}


	public void create(String parent,String name) throws JSONException {
		
		
		JSONObject jo=new JSONObject();
		jo.put("parent", parent);
		jo.put("name", name);
		
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
					Folder folder=gson.fromJson(res.getJSONObject("new_folder").toString(), Folder.class);
					//this works even when gsonResObject contain more members than the local Files.class
					Log.d("onresponse",folder.toString());
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
					Folder folder=gson.fromJson(res.getJSONObject("new_folder").toString(), Folder.class);
					//this works even when gsonResObject contain more members than the local Files.class
					Log.d("onresponse",folder.toString());
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
					Folder folder=gson.fromJson(res.getJSONObject("new_folder").toString(), Folder.class);
					//this works even when gsonResObject contain more members than the local Files.class
					Log.d("onresponse",folder.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		};

		mQueue.add(new JsonObjectRequest(Method.POST,url, jo ,listener,errorListener));
	}

	public void delete(String folderPath) throws JSONException {
		
		
		String url=rootUrl+"delete?target="+folderPath;
		
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
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		mQueue.add(new JsonObjectRequest(Method.DELETE,url, null ,listener,errorListener));
	}

}
