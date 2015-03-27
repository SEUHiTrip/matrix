package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.DisplayAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.ScaleAnimation;
import seu.lab.matrix.animation.TranslationAnimation;
import seu.lab.matrix.controllers.AppController;
import seu.lab.matrix.controllers.FilesController;
import seu.lab.matrix.controllers.FolderController;
import seu.lab.matrix.controllers.VideoController;
import seu.lab.matrix.controllers.WindowController;

import android.os.Bundle;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public abstract class AbstractApp {

	public static String TAG = "AbstractApp";
	public static boolean OFFINE = true;

	protected List<Animatable> mAnimatables;
	protected Camera cam;
	protected Object3D ball1;
	protected SceneCallback scene;

	public static AppController appController;
	public static VideoController videoController;
	public static FilesController filesController;
	public static FolderController folderController;
	public static WindowController windowController;
	
	public AbstractApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		mAnimatables = animatables;
		cam = camera;
		this.ball1 = ball1;
		scene = callback;
	}

	protected void pickList(PickGroup[] pickGroups, int start, int end) {
		PickGroup group = null;
		for (int i = start; i < end; i++) {
			group = pickGroups[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				scene.onActivateTilesGroup(group);
			} else {
				scene.onDeactivateTilesGroup(group);
			}
		}
	}

	protected void slideList(boolean slideLeft, Object3D[] pre, Object3D[] cur,
			int strip, boolean needScale, boolean needDisplayAnimation) {

		SimpleVector moveDir;
		SimpleVector presetDir;

		if (slideLeft) {
			moveDir = new SimpleVector(0, -strip, 0);
			presetDir = new SimpleVector(0, strip, 0);
		} else {
			moveDir = new SimpleVector(0, strip, 0);
			presetDir = new SimpleVector(0, -strip, 0);
		}

		for (Object3D object3d : cur) {
			object3d.translate(presetDir);
		}

		mAnimatables.add(new TranslationAnimation("", pre, moveDir, null));
		mAnimatables.add(new TranslationAnimation("", cur, moveDir, null));

		if (needDisplayAnimation) {
			mAnimatables.add(new DisplayAnimation(pre, "", true));
			mAnimatables.add(new DisplayAnimation(cur, "", false));
		}

		if (needScale)
			mAnimatables.add(new ScaleAnimation(pre, "", 1f));

	}

	abstract public void initObj(String name, Object3D object3d);

	abstract public void postInitObj();

	abstract public void onCreate();

	abstract public void onDestory();

	abstract public void onOpen(Bundle bundle);

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

	abstract public boolean onToggleFullscreen();

}
