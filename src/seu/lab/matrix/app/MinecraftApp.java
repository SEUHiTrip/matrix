package seu.lab.matrix.app;

import java.util.List;

import org.json.JSONException;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class MinecraftApp extends HeadControlApp{

	public MinecraftApp(CardboardView cardboardView,
			List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(cardboardView, 6001, app_name.game_minecraft, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onOpen(Bundle bundle) {
		scene.onScript("我们使用磁铁感应进入全屏模式");
		super.onOpen(bundle);
	}
	
	@Override
	public void onClose(Runnable runnable) {
		scene.onScript("沉浸式飞车游戏也很棒\n让我们体验一下");
		super.onClose(runnable);
	}
	
}
