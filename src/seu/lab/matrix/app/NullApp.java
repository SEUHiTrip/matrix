package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class NullApp extends AbstractApp{

	public NullApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initObj(String name, Object3D object3d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postInitObj() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeft() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLongPress() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDoubleTap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose(Runnable runnable) {
		if(runnable != null)runnable.run();
	}

}
