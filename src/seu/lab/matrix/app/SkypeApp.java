package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class SkypeApp extends SimpleScreenApp{

	public SkypeApp(List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(app_name.skype, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		scene.onScript("好的，现在我们返回宝藏岛\n就可以同时看到3个岛的工作状态");
		super.onOpen(bundle);
	}
	
	
	
}
