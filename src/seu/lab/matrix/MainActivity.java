package seu.lab.matrix;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionType;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	public static final String TAG = "MainActivity";

	public static Class<?> iDisplayerClass = Framework3DMatrixActivity.class;
	
	private RequestQueue mQueue = null;

	private Gson gson = new Gson();
	
	private ImageView imageView;
	
	class Folder{
		public String res = "";
		public String name;
		public String location;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		Button single = (Button) findViewById(R.id.connect_btn_single);
		Button duel = (Button) findViewById(R.id.connect_btn_duel);
		Button cardboardButton = (Button) findViewById(R.id.cardboard_btn);
		Button connetButton = (Button) findViewById(R.id.connect_btn);
		Button testButton = (Button) findViewById(R.id.test);

		imageView = (ImageView) findViewById(R.id.image_view);
		
		single.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Framework3DMatrixActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(ConnectionType.Single.ordinal()));
				startActivity(intent);
				finish();
			}
		});
		
		duel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Framework3DMatrixActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(ConnectionType.Duel.ordinal()));
				startActivity(intent);
				finish();
			}
		});
		
		cardboardButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MatrixActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		connetButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					testVolley();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		testButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), TestActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		mQueue = Volley.newRequestQueue(getApplicationContext());
	}
	
	public void testVolley() throws JSONException {
		
		Folder folder = new Folder();
		folder.name = "ff1";
		folder.location = "ll1";
		
		String url = "http://192.168.1.102:50305/api/test";
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
				Folder folder = gson.fromJson(res.toString(), Folder.class);
				
                Log.d(TAG, "onResponse : " + folder.res);
			}
		};
		
		Listener<Bitmap> imgListener = new Listener<Bitmap>() {

			@Override
			public void onResponse(Bitmap bitmap) {
                Log.d(TAG, "onResponse : " + bitmap.getWidth() + "x" + bitmap.getHeight());
                imageView.setImageBitmap(bitmap);
			}
		};

		//mQueue.add(new JsonObjectRequest(Method.POST, url, new JSONObject(gson.toJson(folder)), listener, errorListener));
		
		mQueue.add(new ImageRequest(url, imgListener, 2000, 2000, Config.ARGB_8888, errorListener));
		
		mQueue.start();
	}
}
