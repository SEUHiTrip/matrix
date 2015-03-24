package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class IEApp extends SimpleScreenApp{

	public IEApp(app_name aName, List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(aName, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}


}
