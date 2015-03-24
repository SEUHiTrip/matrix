package seu.lab.matrix.app;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import seu.lab.matrix.animation.Animatable;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class AbstractScreenApp extends AbstractApp{
	
	class DefaulteErrorListener implements ErrorListener{
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e(TAG, "onError : " + error.toString());
			
			scene.onAppFail();
		}
	};

	class DefaultListener implements Listener<JSONObject> {
		@Override
		public void onResponse(JSONObject res) {
			Log.d(TAG, "onResponse : " + res.toString());
			
			try {
				if(res.getString("result").equals("ok")){
					onOk();
					return;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			onErr();
		}
		
		protected void onOk(){
			scene.onCallScreen();
			scene.onAppReady();
		}
		
		protected void onErr(){
			scene.onAppFail();
		}
	};
	
	DefaulteErrorListener defaultErrorListener = new DefaulteErrorListener();
	DefaultListener defaultListener = new DefaultListener();
	
	ErrorListener closeErrorListener = new ErrorListener(){
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e(TAG, "onError : " + error.toString());
		}
	};
	
	Listener<JSONObject> closeListener = new Listener<JSONObject>() {
		public void onResponse(JSONObject res) {
			Log.d(TAG, "onResponse : " + res.toString());
		}
	};
	
	public AbstractScreenApp(List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
	}

	@Override
	public void initObj(String name, Object3D object3d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postInitObj() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeft() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLongPress() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDoubleTap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose(Runnable runnable) {
		scene.onHideScreen(runnable);
		scene.onAppClosed();
	}

	@Override
	public void onOpen(Bundle bundle) {
		scene.onCallScreen();
		scene.onAppReady();
	}

}
