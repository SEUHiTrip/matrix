package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.DisplayAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.TranslationAnimation;

import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public abstract class AbstractApp {

	public static String TAG = "AbstractApp";
	
	protected List<Animatable> mAnimatables;
	protected Camera cam;
	protected Object3D ball1;
	protected SceneCallback scene;
	
	public AbstractApp(List<Animatable> animatables, SceneCallback callback, Camera camera, Object3D ball1){
		mAnimatables = animatables;
		cam = camera;
		this.ball1 = ball1;
		scene = callback;
	}
	
	abstract public void initObj(String name, Object3D object3d);
	abstract public void postInitObj();
	
	abstract public void onCreate();
	abstract public void onDestory();
	
	abstract public void onOpen();
	abstract public void onShown();
	abstract public void onHide();
	abstract public void onClose(Runnable runnable);

	abstract public void onPick();
	abstract public void onLeft();
	abstract public void onRight();
	abstract public void onUp();
	abstract public void onDown();
	abstract public void onLongPress();
	abstract public void onDoubleTap();
	abstract public void onSingleTap();
	
}
