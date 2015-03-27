package seu.lab.matrix.app;

import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.TranslationAnimation;
import android.os.Bundle;
import android.util.Log;

import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public interface SceneCallback {

	void onAppReady();
	void onAppFail();
	void onAppClosed();
	void onCallScreen();
	void onHideScreen(final Runnable runnable);
	void onCallCurtain(String tex);
	void onHideCurtain();
	void onHideObj(Object3D[] cur, boolean displayAnimation, final Runnable runnable);
	void onOpenApp(final int idx, Bundle bundle);
	void onActivateTilesGroup(PickGroup group);
	void onDeactivateTilesGroup(PickGroup group);
	int getScreenIdx();
	boolean isLookingAtScreen();
	void onSwitchMode(ConnectionMode mode);
	void onSceneToggleFullscreen();

}
