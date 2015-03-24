package seu.lab.matrix.app;

import java.util.List;

import org.json.JSONException;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController;
import seu.lab.matrix.controllers.AppController.app_name;

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
	
}
