package seu.lab.matrix;

import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity.ConnectionType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button single = (Button) findViewById(R.id.connect_btn_single);
		Button duel = (Button) findViewById(R.id.connect_btn_duel);
		single.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ConnectionActivity.class);
				intent.putExtra("mode", new ConnectionActivity.ConnectionMode(ConnectionType.Single.ordinal()));
				startActivity(intent);
				finish();
			}
		});
		
		duel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ConnectionActivity.class);
				intent.putExtra("mode", new ConnectionActivity.ConnectionMode(ConnectionType.Duel.ordinal()));
				startActivity(intent);
				finish();
			}
		});
		
	}
}
