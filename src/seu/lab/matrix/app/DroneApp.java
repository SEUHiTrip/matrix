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
	
	public DroneApp(CardboardView cardboardView,
			List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(cardboardView, 6004, app_name.drone, animatables, callback, camera, ball1);
		SLEEPTIME = 4000;
	}
	
	@Override
	public void onOpen(Bundle bundle) {
		scene.onScript("你仅仅只需要坐在原地\n通过头部的转动控制无人机的转动\n通过低头也抬头控制无人机的移动\n就可以轻松地看到你平时难以看到的画面");
		super.onOpen(bundle);
	}
	
	@Override
	public void onClose(Runnable runnable) {
		scene.onScript("在虚拟空间\n我们当然不会受屏幕大小的限制啦\n所以用它来看电影也很不错");
		super.onClose(runnable);
	}
	
}
