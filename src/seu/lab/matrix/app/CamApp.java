package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class CamApp extends HeadControlApp{

	public CamApp(CardboardView cardboardView,
			List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(cardboardView, 6003, app_name.cam, animatables, callback, camera, ball1);

	}
}
