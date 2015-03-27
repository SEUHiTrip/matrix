package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.DisplayAnimation;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.ScaleAnimation;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;
import seu.lab.matrix.obj.PictureInfo;

import android.os.Bundle;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class PicApp extends AbstractApp {

	final static int PIC_COUNT_PER_PAGE = 6;

	public int mPicPageIdx = 0;
	public int mCurrentPic = 0;
	public boolean isPicScrShown = false;

	public PicApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	private Object3D[] picScrs = new Object3D[2];
	PickGroup[] mPickGroupLists = new PickGroup[6 + 2 + 1];
	private Map<String, Object3D> clickableLists = new HashMap<String, Object3D>();

	private static LiveTileAnimation[] mPicListTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null), };
	
	private String picName=null;

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

	}

	@Override
	public void onShown() {
		toggleList(true, 0, 6 + 3);
		openPic(1);
	}

	@Override
	public void onHide() {
		toggleList(false, 0, 6 + 3);
		scene.onHideObj(picScrs, true, null);
	}

	@Override
	public void onLeft() {
		slidePic(true);
	}

	@Override
	public void onRight() {
		slidePic(false);
	}

	@Override
	public void onUp() {
		scalePic(true);
	}

	@Override
	public void onDown() {
		scalePic(false);
	}

	@Override
	public void onLongPress() {

	}

	@Override
	public void onDoubleTap() {
		PickGroup group;
		Object3D object3d;
		for (int i = 0; i < 6; i++) {
			group = mPickGroupLists[i];
			object3d = group.group[0];

			if (SceneHelper.isLookingAt(cam, ball1,
					object3d.getTransformedCenter()) > 0.995) {
				openPic(i + PIC_COUNT_PER_PAGE * mPicPageIdx + 1);
			}
		}

		for (int i = 6; i < 8; i++) {
			group = mPickGroupLists[i];
			object3d = group.group[0];

			if (SceneHelper.isLookingAt(cam, ball1,
					object3d.getTransformedCenter()) > 0.995) {

				flipPicList();

			}
		}
	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub

	}

	private void openPic(int i) {

		Log.e(TAG, "openPic: " + i);

		mCurrentPic = i;

		picScrs[0].setVisibility(false);
		picScrs[1].setVisibility(false);
		picScrs[1].clearTranslation();
		picScrs[0].clearTranslation();

		if (i < 10) {
			picScrs[0].setTexture("p_" + i);
			picName="p_" + i;
		} else {
			picScrs[0].setTexture("p_" + (char) ('a' + (i - 10)));
			picName="p_" + (char) ('a' + (i - 10));
		}
		
		
		
		
		
		if(picName==null)
			SceneHelper.drawText("w_opt", new String[] { "hello pic", "line2" });
		else {
			PictureInfo pictureInfo=new PictureInfo(picName,"");
			SceneHelper.drawText("w_opt", new String[] { pictureInfo.name+"."+pictureInfo.type, 
					 "Width: "+ pictureInfo.width,
					 "Height: "+ pictureInfo.height,
					 "Iso: "+ pictureInfo.iso,
					 "Duration: "+ pictureInfo.duration,
					 "Aperture: "+ pictureInfo.aperture,
					 "Size: "+ pictureInfo.size});
		}
		
		
		
		
		

		picScrs[0].translate(-3, 0, 0);

		mAnimatables
				.add(new TranslationAnimation("",
						new Object3D[] { picScrs[0] },
						new SimpleVector(3, 0, 0), null));

		mAnimatables.add(new DisplayAnimation(new Object3D[] { picScrs[0] },
				"", false));

		if (picScrs[0].getScale() != 1f) {
			mAnimatables.add(new ScaleAnimation(new Object3D[] { picScrs[0] },
					"", 1f));
		}

		// TODO gjw draw pic desc
	}

	private void slidePic(boolean slideLeft) {

		if (!(picScrs[0].getVisibility() || picScrs[1].getVisibility()))
			return;

		mCurrentPic += slideLeft ? -1 : 1;
		mCurrentPic = mCurrentPic < 1 ? 12 : (mCurrentPic > 12 ? 1
				: mCurrentPic);

		Object3D[] pre = null, cur = null;
//		SimpleVector moveDir;
//		SimpleVector presetDir;
//
//		if (slideLeft) {
//			moveDir = new SimpleVector(0, -2, 0);
//			presetDir = new SimpleVector(0, 2, 0);
//		} else {
//			moveDir = new SimpleVector(0, 2, 0);
//			presetDir = new SimpleVector(0, -2, 0);
//		}

		if (picScrs[0].getVisibility()) {
			pre = new Object3D[]{picScrs[0]};
			cur = new Object3D[]{picScrs[1]};
		} else if (picScrs[1].getVisibility()) {
			pre = new Object3D[]{picScrs[1]};
			cur = new Object3D[]{picScrs[0]};
		}

		if (mCurrentPic < 10) {
			cur[0].setTexture("p_" + mCurrentPic);
		} else {
			cur[0].setTexture("p_" + (char) ('a' + (mCurrentPic - 10)));
		}
		pre[0].clearTranslation();
		cur[0].clearTranslation();
		cur[0].setScale(1f);

//		cur.translate(presetDir);
//
//		mAnimatables.add(new TranslationAnimation("", new Object3D[] { pre },
//				moveDir, null));
//		mAnimatables.add(new TranslationAnimation("", new Object3D[] { cur },
//				moveDir, null));
//
//		mAnimatables
//				.add(new DisplayAnimation(new Object3D[] { pre }, "", true));
//		mAnimatables
//				.add(new DisplayAnimation(new Object3D[] { cur }, "", false));
//
//		mAnimatables.add(new ScaleAnimation(new Object3D[] { pre }, "", 1f));

		slideList(slideLeft, pre, cur, 2, true, true);
		
		// TODO gjw draw pic desc

	}

	private void scalePic(boolean up) {
		if (!(picScrs[0].getVisibility() || picScrs[1].getVisibility()))
			return;

		Object3D cur = null;
		if (picScrs[0].getVisibility()) {
			cur = picScrs[0];
		} else if (picScrs[1].getVisibility()) {
			cur = picScrs[1];
		}

		float scale = cur.getScale() + (up ? 0.2f : -0.2f);

		mAnimatables.add(new ScaleAnimation(new Object3D[] { cur }, "", scale));

	}

	private void postInitList() {

		for (int i = 0; i < 6; i++) {
			mPickGroupLists[i] = new PickGroup(2);
		}
		for (int i = 6; i < mPickGroupLists.length; i++) {
			mPickGroupLists[i] = new PickGroup(1);
		}

		for (int i = 0; i < mPicListTiles.length; i++) {
			mPicListTiles[i].setFrames(40);
		}

		Object3D tmp;
		tmp = clickableLists.get("l_l1");
		mPicListTiles[0].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[0].group[0] = tmp;
		tmp = clickableLists.get("l_l2");
		mPicListTiles[1].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[1].group[0] = tmp;
		tmp = clickableLists.get("l_l3");
		mPicListTiles[2].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[2].group[0] = tmp;
		tmp = clickableLists.get("l_l4");
		mPicListTiles[3].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[3].group[0] = tmp;
		tmp = clickableLists.get("l_l5");
		mPicListTiles[4].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[4].group[0] = tmp;
		tmp = clickableLists.get("l_l6");
		mPicListTiles[5].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[5].group[0] = tmp;
		tmp = clickableLists.get("l_l7");
		mPicListTiles[0].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[0].group[1] = tmp;
		tmp = clickableLists.get("l_l8");
		mPicListTiles[1].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[1].group[1] = tmp;
		tmp = clickableLists.get("l_l9");
		mPicListTiles[2].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[2].group[1] = tmp;
		tmp = clickableLists.get("l_la");
		mPicListTiles[3].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[3].group[1] = tmp;
		tmp = clickableLists.get("l_lb");
		mPicListTiles[4].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[4].group[1] = tmp;
		tmp = clickableLists.get("l_lc");
		mPicListTiles[5].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[5].group[1] = tmp;

		tmp = clickableLists.get("w_next");
		mPickGroupLists[6].group[0] = tmp;

		tmp = clickableLists.get("w_back");
		mPickGroupLists[7].group[0] = tmp;

		tmp = clickableLists.get("w_opt");
		mPickGroupLists[8].group[0] = tmp;
	}

	private void initLists(String name, Object3D object3d) {

		String tname = name.substring(2, name.indexOf("_Plane"));
		Log.e(TAG, "matching tname:" + tname);

		SceneHelper.printTexture(tname, object3d);

		object3d.setVisibility(false);

		clickableLists.put(tname, object3d);

		if (name.startsWith("x_p_s1lide")) {
			picScrs[0] = object3d;
		} else if (name.startsWith("x_p_s2lide")) {
			picScrs[1] = object3d;
		}
	}

	private void flipPicList() {

		for (int i1 = 0; i1 < mPicListTiles.length; i1++) {
			mPicListTiles[i1].reset();
			// mAnimatables.add(mListTiles[i1]);
		}

		mAnimatables.add(new SeqAnimation(mAnimatables, mPicListTiles));

		mPicPageIdx = (mPicPageIdx + 1) % 2;
	}

	private void toggleList(boolean on, int from, int to) {
		Object3D[] group;
		for (int i = from; i < to; i++) {
			group = mPickGroupLists[i].group;
			for (int j = 0; j < group.length; j++) {
				Log.e(TAG, i + " " + j + " " + (group[j] == null));
				group[j].setVisibility(on);
			}
		}
	}

	@Override
	public void onPick() {
		pickList(mPickGroupLists, 0, mPickGroupLists.length);
	}

	@Override
	public void onOpen(Bundle bundle) {
		onShown();
		scene.onAppReady();
	}

	@Override
	public void onClose(Runnable runnable) {
		toggleList(false, 0, 6 + 3);
		scene.onHideObj(picScrs, true, runnable);
		scene.onAppClosed();
	}

}
