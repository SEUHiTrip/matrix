package seu.lab.matrix;

import org.json.JSONException;
import org.json.JSONObject;

import seu.lab.matrix.ar.ARActivity;
import seu.lab.matrix.bullet.BulletTestActivity;
import seu.lab.matrix.controllers.Confg;
import seu.lab.matrix.red.ColorBlobDetectionActivity;
import seu.lab.matrix.red.ColorTrackActivity;
import seu.lab.matrix.test.MatrixActivity;
import seu.lab.matrix.test.PeopleTestActivity;

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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TAG = "MainActivity";

	static final String ipReg = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]).){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().hide();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Button onlineButton = (Button) findViewById(R.id.scene_online_btn);
		Button offlineButton = (Button) findViewById(R.id.scene_offline_btn);
		Button ipButton = (Button) findViewById(R.id.ip_btn);

		onlineButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Framework3DMatrixActivity.NEED_IDISPLAY = true;
				Intent intent = new Intent(getApplicationContext(),
						SceneActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(
						ConnectionType.Single.ordinal()));
				startActivity(intent);
			}
		});

		offlineButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Framework3DMatrixActivity.NEED_IDISPLAY = false;

				Intent intent = new Intent(getApplicationContext(),
						SceneActivity.class);
				intent.putExtra("mode", new IDisplayConnection.ConnectionMode(
						ConnectionType.Single.ordinal()));
				startActivity(intent);
			}
		});

		ipButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final EditText editText = new EditText(MainActivity.this);
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Server IP for "+Build.MODEL)
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(editText)
						.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								
								if(editText.getText().toString().matches(ipReg)){
									Confg.IP = editText.getText().toString();
									Confg.WEB_API = Confg.WEB_API_PRE + Confg.IP + Confg.WEB_API_SUB;
									Toast.makeText(getApplicationContext(), Confg.WEB_API, Toast.LENGTH_SHORT).show();
								}else {
									Toast.makeText(getApplicationContext(), "not valid", Toast.LENGTH_SHORT).show();
								}

							}
						})
						.setNegativeButton("cancel", null).show();
			}
		});

	}

}
