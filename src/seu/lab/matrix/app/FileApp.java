package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;

public class FileApp extends AbstractApp{

	private World world;
	
	public int mFilePageIdx = 0;

	public FileApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1, World world) {
		super(animatables, callback, camera, ball1);
		this.world = world;
	}

	PickGroup[] mPickGroupFiles = new PickGroup[3];
	PickGroup[] mPickGroupFiles1 = new PickGroup[9];
	PickGroup[] mPickGroupFiles2 = new PickGroup[9];

	PickGroup[] mPickGroupDesks = new PickGroup[5];

	protected Object3D[] desks = null;
	protected Object3D[] files = null;
	protected Object3D[] files1 = null;
	protected Object3D[] files2 = null;
	
	private Map<String, Object3D> clickableFiles = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableDesks = new HashMap<String, Object3D>();

	
	void file() {

		startFrom(desks, 5);
		if(mFilePageIdx == 0){
			startFrom(files1, 5);
			for (int i = 0; i < 9; i++) {
				SimpleVector target = new SimpleVector();
				cam.getPosition(target);
				target.add(new SimpleVector(-3 - 5, 0.8 * (i / 3 - 1),
						0.5 + 0.8 * (i % 3 - 1)));
				files1[i].translate(target.calcSub(files1[i]
						.getTransformedCenter()));
			}
		}else {
			startFrom(files2, 5);
			for (int i = 0; i < 9; i++) {
				SimpleVector target = new SimpleVector();
				cam.getPosition(target);
				target.add(new SimpleVector(-3 - 5, 0.8 * (i / 3 - 1),
						0.5 + 0.8 * (i % 3 - 1)));
				files2[i].translate(target.calcSub(files2[i]
						.getTransformedCenter()));
			}
		}

		mAnimatables.add(new TranslationAnimation("", SceneHelper.to1DArr(new Object3D[][] {
				desks,files1,files2
		}), new SimpleVector(5, 0, 0), null));

	}
	
	void startFrom(Object3D[] object3ds, int distance) {
		for (int i = 0; i < object3ds.length; i++) {
			object3ds[i].clearTranslation();
			object3ds[i].translate(-distance, 0, 0);
			object3ds[i].setVisibility(true);
		}
	}

	private void postInitFile() {
		for (int i = 0; i < mPickGroupFiles.length; i++) {
			mPickGroupFiles[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupDesks.length; i++) {
			mPickGroupDesks[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupFiles1.length; i++) {
			mPickGroupFiles1[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupFiles2.length; i++) {
			mPickGroupFiles2[i] = new PickGroup(1);
		}
		Object3D tmp;
		tmp = clickableDesks.get("f_trash");
		mPickGroupDesks[0].group[0] = tmp;

		tmp = clickableDesks.get("f_i_open");
		mPickGroupDesks[1].group[0] = tmp;

		tmp = clickableDesks.get("f_i_copy");
		mPickGroupDesks[2].group[0] = tmp;

		tmp = clickableDesks.get("f_i_cut");
		mPickGroupDesks[3].group[0] = tmp;

		tmp = clickableDesks.get("f_i_delete");
		mPickGroupDesks[4].group[0] = tmp;

		tmp = clickableFiles.get("f_book_b");
		mPickGroupFiles[0].group[0] = tmp;

		tmp = clickableFiles.get("f_book_s");
		mPickGroupFiles[1].group[0] = tmp;

		tmp = clickableFiles.get("f_b_folder");
		mPickGroupFiles[2].group[0] = tmp;

		desks = new Object3D[clickableDesks.size()];
		files = new Object3D[clickableFiles.size()];
		
		files1 = new Object3D[mPickGroupFiles1.length];
		files2 = new Object3D[mPickGroupFiles2.length];
		
		desks = clickableDesks.values().toArray(desks);
		files = clickableFiles.values().toArray(files);

		for (int i = 0; i < 9; i++) {
			files1[i] = mPickGroupFiles[1].group[0].cloneObject();
			files1[i].setTexture("dummy");
			mPickGroupFiles1[i].group[0] = files1[i];
			world.addObject(files1[i]);
			
			files2[i] = mPickGroupFiles[1].group[0].cloneObject();
			mPickGroupFiles2[i].group[0] = files2[i];
			world.addObject(files2[i]);
		}

	}
	
	

	private void initFile(String name, Object3D object3d) {
		object3d.setVisibility(false);
		// object3d.setAdditionalColor(new RGBColor(100, 100, 100));

		int end = -1;
		end = end == -1 ? name.indexOf("_Plane") : end;
		end = end == -1 ? name.indexOf("_Cube") : end;
		end = end == -1 ? name.indexOf("_Cylinder") : end;
		end = end == -1 ? name.indexOf("_OBJ") : end;

		String tname = name.substring(2, (end == -1 ? name.length() : end));

		Log.e(TAG, "nice: " + tname);

		if (tname.startsWith("f_b")) {
			object3d.getMesh().setLocked(true);
			// printTexture(tname, object3d);
			clickableFiles.put(tname, object3d);
		} else if (tname.startsWith("f_i_")) {
			SceneHelper.printTexture(tname, object3d);
			clickableDesks.put(tname, object3d);
		} else {
			clickableDesks.put(tname, object3d);
		}
	}
	
	private void toggleDesk(boolean on) {
		for (int i = 0; i < desks.length; i++) {
			desks[i].setVisibility(on);
		}
		for (int i = 0; i < files.length; i++) {
			files[i].setVisibility(on);
		}
		for (int i = 0; i < files1.length; i++) {
			files1[i].setVisibility(on);
		}
		for (int i = 0; i < files2.length; i++) {
			files2[i].setVisibility(on);
		}
	}

	@Override
	public void onPick() {
		// TODO
	}

	@Override
	public void initObj(String name, Object3D object3d) {
		initFile(name, object3d);
	}

	@Override
	public void postInitObj() {
		postInitFile();
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
		//toggleDesk(true);
		file();
	}

	@Override
	public void onHide() {
		toggleDesk(false);
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
		onShown();
		scene.onAppReady();
	}

	@Override
	public void onClose(Runnable runnable) {
		scene.onHideObj(SceneHelper.to1DArr(new Object3D[][]{
				desks, files1, files2
		}), false, runnable);
		scene.onAppClosed();
	}
	
}
