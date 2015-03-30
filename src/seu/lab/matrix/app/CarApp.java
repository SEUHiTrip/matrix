package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class CarApp extends HeadControlApp{

	public CarApp(CardboardView cardboardView,
			List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(cardboardView, 6002, app_name.game_car, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		scene.onScript("在开始过程中\n你可以感觉到赛道上\n和两边的东西仿佛扑面而来\n同样，你可以通过头部的转动\n来控制飞机的转向");
		super.onOpen(bundle);
	}
	
	@Override
	public void onClose(Runnable runnable) {
		scene.onScript("使用者除了可以用我们的项目\n去体验虚拟的3D空间以外\n它还可以帮助你去探索真实的世界\n接下来我们将用它来控制一台无人机");
		super.onClose(runnable);
	}
	
}
