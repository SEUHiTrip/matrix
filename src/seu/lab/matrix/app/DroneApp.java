package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.app.AbstractScreenApp.DefaultListener;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class DroneApp extends HeadControlApp{

	boolean canPlay = false;
	
	public DroneApp(CardboardView cardboardView,
			List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(cardboardView, 6004, app_name.drone, animatables, callback, camera, ball1);
		
		defaultListener = new DefaultListener(){
			@Override
			protected void onErr() {
				// TODO Auto-generated method stub
				super.onErr();
			}
			@Override
			protected void onOk() {
				scene.onCallScreen();
				scene.onAppReady();
			}
		};	
	}
	
	@Override
	public void onOpen(Bundle bundle) {
		canPlay = true;
		super.onOpen(bundle);
	}

	@Override
	public boolean onToggleFullscreen() {
		scene.onSceneToggleFullscreen();
		
		if(canPlay){
			canPlay = false;
			stopped = false;
			(curThread = new Thread(getHeadTransform){
				@Override
				public void run() {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					super.run();
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					superClose();
				}
			}).start();
			
		}else {
			stopped = true;
			if(curThread.isAlive()){
				curThread.interrupt();
			}else {
				superClose();
			}
		}

		return true;
	}
	
	
	
}
