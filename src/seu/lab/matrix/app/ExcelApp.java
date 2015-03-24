package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class ExcelApp extends SimpleScreenApp{

	public ExcelApp(List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(app_name.office_excel, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

}
