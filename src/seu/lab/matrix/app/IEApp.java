package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class IEApp extends MouseControlApp{

	public IEApp(List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(app_name.internet, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		scene.onScript("，首先我们在这个工作区打开IE浏览器\n方便进行数据的查阅\n接下来去另一个工作区打开PPT");
		super.onOpen(bundle);
	}
	
}
