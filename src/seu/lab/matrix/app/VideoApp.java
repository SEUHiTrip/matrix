package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;

import seu.lab.matrix.Framework3DMatrixActivity;
import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.controllers.AppController;
import seu.lab.matrix.obj.VideoInfo;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class VideoApp extends AbstractScreenApp {

	final static int VIDEO_COUNT_PER_PAGE = 4;

	boolean isVideoPlaying = false;
	private String videoName = null;

	public int mVideoPageIdx;
	final static String[] videoUrl = new String[] { "c:\\bigHero.mkv",

	};

	PickGroup[] mPickGroupLists = new PickGroup[4 + 2 + 1];
	private Map<String, Object3D> clickableLists = new HashMap<String, Object3D>();

	public VideoApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	private static LiveTileAnimation[] mVideoListTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null), };

	private DefaultListener playListener = new DefaultListener() {
		protected void onErr() {
			isVideoPlaying = false;
		}

		protected void onOk() {
			isVideoPlaying = true;
			scene.onHideCurtain();
			scene.onCallScreen();
		}
	};

	@Override
	public void initObj(String name, Object3D object3d) {
		initLists(name, object3d);
	}

	@Override
	public void postInitObj() {
		postInitList();
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
		toggleList(true, 0, 7);
		SceneHelper.drawText("w_opt", new String[] { "Video", "" });
		// TODO
	}

	@Override
	public void onHide() {
		toggleList(false, 0, 7);

	}

	@Override
	public void onClose(Runnable runnable) {

		scene.onScript("我们不能仅仅通过它进行娱乐\n既然屏幕大小不会受到限制\n当然数量也不会受到限制了\n所以我们可以自由的扩充自己的工作区\n与团队进行更好的协作");

		// scene.onStopRed();
		// scene.onStartDolphin();

		if (isVideoPlaying) {
			try {
				videoController.close(scene.getScreenIdx());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		onHide();
		scene.onHideCurtain();
		scene.onHideScreen(runnable);
		scene.onAppClosed();
		// scene.onSwitchMode(new ConnectionMode(1));

	}

	@Override
	public void onLeft() {
		if (!isVideoPlaying)
			return;
		try {
			videoController.backward(scene.getScreenIdx());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onRight() {
		if (!isVideoPlaying)
			return;
		try {
			videoController.forward(scene.getScreenIdx());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public boolean onDoubleTap() {

		PickGroup group = null;
		Object3D object3d;

		if (isVideoPlaying && scene.isLookingAtScreen()) {
			try {
				videoController.continue_pause(scene.getScreenIdx());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		for (int i = 0; i < 4; i++) {
			group = mPickGroupLists[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				openVideo(i + VIDEO_COUNT_PER_PAGE * mVideoPageIdx + 1);
				return true;
			}
		}

		for (int i = 4; i < 6; i++) {
			group = mPickGroupLists[i];
			object3d = group.group[0];

			if (SceneHelper.isLookingAt(cam, ball1,
					object3d.getTransformedCenter()) > 0.995) {

				flipVideoList();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPick() {
		pickList(mPickGroupLists, 0, mPickGroupLists.length);
	}

	@Override
	public void onOpen(Bundle bundle) {

		// try {
		// appController.open(scene.getScreenIdx(),
		// AppController.app_name.video, defaultErrorListener,
		// listener);
		// } catch (JSONException e) {
		// Log.e(TAG, e.toString());
		// }
		scene.onScript("我们撑满眼眶的画面让你感觉似乎是在电影院\n红外操控");
		onShown();
		scene.onCallCurtain("b_v3ideo");
		scene.onAppReady();

		if (Framework3DMatrixActivity.isDisplayConnected()
				&& Framework3DMatrixActivity.IS_PRESENTING) {
			scene.onStopDolphin();
			scene.onStartRed();
						
		}
	}

	private void openVideo(int i) {

		// TODO gjw draw video desc

		if (isVideoPlaying) {
			scene.onCallCurtain("b_v3ideo");
			scene.onHideScreen(null);
			try {
				videoController.close(scene.getScreenIdx());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		final boolean tmp = isVideoPlaying;

		new Thread() {

			public void run() {
				try {
					if (tmp) {
						sleep(2000);
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					videoController.play(scene.getScreenIdx(), videoUrl[0],
							defaultErrorListener, playListener);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

		String[] temp = videoUrl[0].split("\\\\");
		videoName = temp[temp.length - 1];

		if (videoName == null)
			SceneHelper
					.drawText("w_opt", new String[] { "hello pic", "line2" });
		else {
			VideoInfo videoInfo = new VideoInfo(videoName, "");
			SceneHelper.drawText("w_opt", new String[] { videoInfo.name,
					"Width: " + videoInfo.width, "Height: " + videoInfo.height,
					"Framerate: " + videoInfo.framerate,
					"Length: " + videoInfo.length, "Size: " + videoInfo.size });
		}

		isVideoPlaying = false;

	}

	private void toggleList(boolean on, int from, int to) {
		Object3D[] group;
		for (int i = from; i < to; i++) {
			group = mPickGroupLists[i].group;
			for (int j = 0; j < group.length; j++) {
				group[j].setVisibility(on);
			}
		}
	}

	private void postInitList() {

		for (int i = 0; i < 4; i++) {
			mPickGroupLists[i] = new PickGroup(2);
		}
		for (int i = 4; i < mPickGroupLists.length; i++) {
			mPickGroupLists[i] = new PickGroup(1);
		}

		for (int i = 0; i < mVideoListTiles.length; i++) {
			mVideoListTiles[i].setFrames(40);
		}

		Object3D tmp;

		for (int i = 0; i < 8; i++) {
			tmp = clickableLists.get("l_m" + (i + 1));

			SimpleVector simpleVector;

			if (i % 2 == 0) {
				simpleVector = new SimpleVector(3.732, 1, 0);
			} else {
				simpleVector = new SimpleVector(0, 0, 1);
			}

			if (i / 4 == 0) {
				mVideoListTiles[i % 4].setTile1(tmp, simpleVector);
			} else {
				mVideoListTiles[i % 4].setTile2(tmp, simpleVector);
			}

			mPickGroupLists[i % 4].group[(i / 4)] = tmp;
		}

		tmp = clickableLists.get("w_next");
		mPickGroupLists[4].group[0] = tmp;

		tmp = clickableLists.get("w_back");
		mPickGroupLists[5].group[0] = tmp;

		tmp = clickableLists.get("w_opt");
		mPickGroupLists[6].group[0] = tmp;
	}

	private void initLists(String name, Object3D object3d) {

		String tname = name.substring(2, name.indexOf("_Plane"));
		Log.e(TAG, "matching tname:" + tname);

		SceneHelper.printTexture(tname, object3d);

		object3d.setVisibility(false);

		clickableLists.put(tname, object3d);

	}

	private void flipVideoList() {

		for (int i1 = 0; i1 < mVideoListTiles.length; i1++) {
			mVideoListTiles[i1].reset();
			// mAnimatables.add(mListTiles[i1]);
		}

		mAnimatables.add(new SeqAnimation(mAnimatables, mVideoListTiles));

		mVideoPageIdx = (mVideoPageIdx) + 1 % 2;
	}

	@Override
	public void onMove(Point p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPress(Point p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRaise(Point p) {
		// TODO Auto-generated method stub

	}
}
