package seu.lab.matrix.app;

import java.util.List;

import org.json.JSONException;
import org.opencv.core.Point;

import seu.lab.matrix.Framework3DMatrixActivity;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class SimpleScreenApp extends AbstractScreenApp{

	private app_name aName;
	
	public SimpleScreenApp(app_name aName, List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		this.aName = aName;
	}

	@Override
	public void onOpen(Bundle bundle) {
		
		if(!Framework3DMatrixActivity.isDisplayConnected()){
			super.onOpen(bundle);
			return;
		}
		
		try {
			appController.open(scene.getScreenIdx(),
					aName, defaultErrorListener,
					defaultListener);
			windowController.setMouse(scene.getScreenIdx());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	@Override
	public void onShown() {
		if(Framework3DMatrixActivity.isDisplayConnected()){
			try {
				windowController.setMouse(scene.getScreenIdx());
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}

		super.onShown();
	}
	
	@Override
	public void onClose(Runnable runnable) {
		if(Framework3DMatrixActivity.isDisplayConnected()){
			try {
				appController.close(scene.getScreenIdx(),
						closeErrorListener,
						closeListener);
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}

		super.onClose(runnable);
	}

	@Override
	public void onMove(Point p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPress(Point p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRaise(Point p) {
		// TODO Auto-generated method stub
		
	}
	
}
