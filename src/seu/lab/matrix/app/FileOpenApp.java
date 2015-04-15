package seu.lab.matrix.app;

import java.util.List;

import org.json.JSONException;
import org.opencv.core.Point;

import seu.lab.matrix.Framework3DMatrixActivity;
import seu.lab.matrix.animation.Animatable;

import android.os.Bundle;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class FileOpenApp extends AbstractScreenApp{

	boolean fileOpened = false;
	
	DefaultListener fileListener = new DefaultListener(){
		protected void onOk() {
			fileOpened = true;
			super.onOk();
		}
		protected void onErr() {
			fileOpened = false;
			super.onErr();
		}
	};
	
	public FileOpenApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		
		Log.e(TAG, "onOpen file open");
		
		if(!Framework3DMatrixActivity.isDisplayConnected()){
			super.onOpen(bundle);
			return;
		}
		
		if(bundle == null){
			scene.onAppFail();
			return;
		}
		String file = bundle.getString("file");
		if(file == null){
			scene.onAppFail();
			return;
		}
		
		try {
			filesController.open(scene.getScreenIdx(), file, defaultErrorListener, fileListener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		if(fileOpened && Framework3DMatrixActivity.isDisplayConnected()){
			try {
				filesController.close(scene.getScreenIdx(), "");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
