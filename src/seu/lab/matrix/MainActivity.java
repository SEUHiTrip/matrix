package seu.lab.matrix;

import org.json.JSONException;
import org.json.JSONObject;

import seu.lab.matrix.ar.ARActivity;
import seu.lab.matrix.bullet.BulletTestActivity;
import seu.lab.matrix.red.ColorBlobDetectionActivity;
import seu.lab.matrix.red.ColorTrackActivity;
import seu.lab.matrix.test.MatrixActivity;

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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	public static final String TAG = "MainActivity";

	private RequestQueue mQueue = null;

	private Gson gson = new Gson();

	private ImageView imageView;

	class Folder {
		public String res = "";
		public String name;
		public String location;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().hide();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Button single = (Button) findViewById(R.id.connect_btn_single);
		Button duel = (Button) findViewById(R.id.connect_btn_duel);
		Button side = (Button) findViewById(R.id.connect_btn_sidebyside);

		Button connetButton = (Button) findViewById(R.id.connect_btn);
		Button testButton = (Button) findViewById(R.id.test);
		Button trackButton = (Button) findViewById(R.id.track_btn);
		Button arButton = (Button) findViewById(R.id.ar_btn);
		Button sceneButton=(Button)findViewById(R.id.scene_btn);

		imageView = (ImageView) findViewById(R.id.image_view);

		single.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						SceneActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(
						ConnectionType.Single.ordinal()));
				startActivity(intent);
			}
		});

		duel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						SceneActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(
						ConnectionType.Duel.ordinal()));
				startActivity(intent);
			}
		});

		side.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						SceneActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(
						ConnectionType.SideBySide.ordinal()));
				startActivity(intent);
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
				Intent intent = new Intent(getApplicationContext(),
						PeopleTestActivity.class);
				startActivity(intent);
			}
		});

		trackButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(),
						ColorTrackActivity.class);
				startActivity(intent);
			}
		});

		arButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(),
						ARActivity.class);
				startActivity(intent);
			}
		});
		
		sceneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(),
						SceneActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(
						ConnectionType.Single.ordinal()));
				startActivity(intent);
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
				Log.d(TAG,
						"onResponse : " + bitmap.getWidth() + "x"
								+ bitmap.getHeight());
				imageView.setImageBitmap(bitmap);
			}
		};

		// mQueue.add(new JsonObjectRequest(Method.POST, url, new
		// JSONObject(gson.toJson(folder)), listener, errorListener));

		mQueue.add(new ImageRequest(url, imgListener, 2000, 2000,
				Config.ARGB_8888, errorListener));

		mQueue.start();
	}
}
