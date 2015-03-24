package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PickGroup;

public class LauncherApp extends AbstractApp{

	public LauncherApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1) {
		super(animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	private Map<String, Object3D> clickableBoards = new HashMap<String, Object3D>();
	
	PickGroup[] mPickGroupBoards = new PickGroup[11+1];

	private LiveTileAnimation[] mBoardTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("minecraft"), new LiveTileAnimation(""),
			new LiveTileAnimation(""), new LiveTileAnimation(""), };

	@Override
	public void initObj(String name, Object3D object3d) {
		initBoard(name, object3d);
	}

	@Override
	public void postInitObj() {
		postInitBoard();
		for (int i = 0; i < mBoardTiles.length; i++) {
			mAnimatables.add(mBoardTiles[i]);
		}
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
	public void onClose(Runnable runnable) {
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
		
		Log.e(TAG, "onDoubleTap");
		
		PickGroup group;
		for (int i = 0; i < mPickGroupBoards.length; i++) {
			group = mPickGroupBoards[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				scene.onOpenApp(i, null);
			}
		}
	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub
		
	}

	public void initBoard(String name, Object3D object3d) {

		if(name.startsWith("x_drone")){
			clickableBoards.put("x_drone", object3d);
			return;
		}
		
		TextureManager tm = TextureManager.getInstance();
		String tname = name.substring(2, name.indexOf("_Plane"));
		object3d.clearAdditionalColor();

		Log.e(TAG, "matching tname:" + tname);

		clickableBoards.put(tname, object3d);

		SceneHelper.printTexture(tname, object3d);
	}
	
	public void postInitBoard() {
		for (int i = 0; i < 4; i++) {
			mPickGroupBoards[i] = new PickGroup(2);
		}
		for (int i = 4; i < mPickGroupBoards.length; i++) {
			mPickGroupBoards[i] = new PickGroup(1);
		}

		Object3D tmp;
		tmp = clickableBoards.get("b_minecraft");
		mBoardTiles[0].setTile1(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[0].group[0] = tmp;

		tmp = clickableBoards.get("b_m2inecraft");
		mBoardTiles[0].setTile2(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[0].group[1] = tmp;

		tmp = clickableBoards.get("b_car");
		mBoardTiles[1].setTile1(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[1].group[0] = tmp;

		tmp = clickableBoards.get("b_c2ar");
		mBoardTiles[1].setTile2(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[1].group[1] = tmp;

		tmp = clickableBoards.get("b_video");
		mBoardTiles[2].setTile1(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[2].group[0] = tmp;

		tmp = clickableBoards.get("b_v2ideo");
		mBoardTiles[2].setTile2(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[2].group[1] = tmp;

		tmp = clickableBoards.get("b_pic");
		mBoardTiles[3].setTile1(tmp, new SimpleVector(1.732, -1, 0));
		mPickGroupBoards[3].group[0] = tmp;

		tmp = clickableBoards.get("b_p2ic");
		mBoardTiles[3].setTile2(tmp, new SimpleVector(1.732, -1, 0));
		mPickGroupBoards[3].group[1] = tmp;

		tmp = clickableBoards.get("b_skype");
		mPickGroupBoards[4].group[0] = tmp;
		tmp = clickableBoards.get("b_ie");
		mPickGroupBoards[5].group[0] = tmp;
		tmp = clickableBoards.get("b_file");
		mPickGroupBoards[6].group[0] = tmp;
		tmp = clickableBoards.get("b_word");
		mPickGroupBoards[7].group[0] = tmp;
		tmp = clickableBoards.get("b_excel");
		mPickGroupBoards[8].group[0] = tmp;
		tmp = clickableBoards.get("b_ppt");
		mPickGroupBoards[9].group[0] = tmp;
		tmp = clickableBoards.get("b_null");
		mPickGroupBoards[10].group[0] = tmp;
		
		tmp = clickableBoards.get("x_drone");
		mPickGroupBoards[11].group[0] = tmp;
	}

	@Override
	public void onPick() {
		pickList(mPickGroupBoards, 0, mPickGroupBoards.length);
	}

	@Override
	public void onOpen(Bundle bundle) {
		// TODO Auto-generated method stub
		
	}
	
}
