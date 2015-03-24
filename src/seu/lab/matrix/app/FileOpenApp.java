package seu.lab.matrix.app;

import java.util.List;

import org.json.JSONException;

import seu.lab.matrix.animation.Animatable;

import android.os.Bundle;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class FileOpenApp extends AbstractScreenApp{

	DefaultListener fileListener = new DefaultListener(){
		
	};
	
	public FileOpenApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		if(bundle == null){
			scene.onAppFail();
			return;
		}
		String file = bundle.getString("file");
		if(file != null){
			scene.onAppFail();
			return;
		}
		
		try {
			filesController.open(scene.getScreenIdx(), file, defaultErrorListener, fileListener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onOpen(null);
	}
	
	@Override
	public void onClose(Runnable runnable) {
		// TODO Auto-generated method stub
		super.onClose(runnable);
	}
	
}
