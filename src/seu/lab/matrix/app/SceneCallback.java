package seu.lab.matrix.app;

import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.TranslationAnimation;
import android.util.Log;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public interface SceneCallback {

	void onAppReady();
	void onAppFail();
	void onAppClosed();
	void onCallScreen();
	void onHideScreen(final Runnable runnable);
	void onHideObj(Object3D[] cur, boolean displayAnimation, final Runnable runnable);
	void onOpenApp(final int idx);
	void onActivateTilesGroup(PickGroup group);
	void onDeactivateTilesGroup(PickGroup group);
	
}
