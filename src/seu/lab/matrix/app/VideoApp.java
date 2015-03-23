package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.SeqAnimation;

import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class VideoApp extends SimpleScreenApp{
	
	final static int VIDEO_COUNT_PER_PAGE = 4;

	public int mVideoPageIdx;
	
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
		SceneHelper.drawText("w_opt", new String[] { "hello video", "line2" });

	}

	@Override
	public void onHide() {
		toggleList(false, 0, 7);
		
	}

	@Override
	public void onClose(Runnable runnable) {
		onHide();
		scene.onHideScreen(runnable);
		scene.onAppClosed();
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
		
		PickGroup group = null;
		Object3D object3d;
		for (int i = 0; i < 4; i++) {
			group = mPickGroupLists[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				openVideo(i + VIDEO_COUNT_PER_PAGE * mVideoPageIdx
						+ 1);
				break;
			}
		}
		
		for (int i = 4; i < 6; i++) {
			group = mPickGroupLists[i];
			object3d = group.group[0];

			if (SceneHelper.isLookingAt(cam, ball1,
					object3d.getTransformedCenter()) > 0.995) {

				flipVideoList();
			}
		}

	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPick() {
		pickList();
	}
	
	@Override
	public void onOpen() {
		onShown();
		super.onOpen();
	}
	

	private void openVideo(int i) {

		// TODO gjw draw video desc

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
	
	private void pickList() {
		PickGroup group = null;
		for (int i = 0; i < mPickGroupLists.length; i++) {
			group = mPickGroupLists[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				scene.onActivateTilesGroup(group);
			} else {
				scene.onDeactivateTilesGroup(group);
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

		mVideoPageIdx = mVideoPageIdx + 1 % 2;
	}
}
